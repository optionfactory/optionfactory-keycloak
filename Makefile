BRIDGE_SUBNET=172.18.26.0/24

build:
	mvn clean package
bump:
	mvn versions:set -DgenerateBackupPoms=false
publish-central:
	mvn clean deploy -Pcentral
check-updates:
	mvn org.codehaus.mojo:versions-maven-plugin:2.16.2:display-dependency-updates  -Dmaven.version.ignore='.*-.*,.*\.CR[1-9],.*\.Beta[1-9]'
	mvn org.codehaus.mojo:versions-maven-plugin:2.16.2:display-plugin-updates -Dmaven.version.ignore='.*-.*'


local-network: 
	-@docker network create -o "com.docker.network.bridge.name"="keycloak" --subnet ${BRIDGE_SUBNET} -d bridge keycloak

local-keycloak: local-network
	$(eval CURRENT_VERSION := $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout))
	docker run -ti --rm \
		--network keycloak \
		--name keycloak \
		--ip 172.18.26.2 \
		-e DEBUG=true \
		-e DEBUG_PORT="8787" \
		-e JAVA_TOOL_OPTIONS="--add-opens=java.base/java.lang=ALL-UNNAMED" \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-providers/target/optionfactory-keycloak-providers-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-providers.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-remoting/target/optionfactory-keycloak-remoting-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-remoting.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-email-sender/target/optionfactory-keycloak-email-sender-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-email-sender.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-login-stats/target/optionfactory-keycloak-login-stats-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-login-stats.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-api-provisioning/target/optionfactory-keycloak-api-provisioning-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-api-provisioning.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-online-access/target/optionfactory-keycloak-online-access-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-online-access.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-idp-apple/target/optionfactory-keycloak-idp-apple-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-idp-apple.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-authenticators/target/optionfactory-keycloak-authenticators-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-authenticators.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-ldap/target/optionfactory-keycloak-ldap-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-ldap.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-themes/target/optionfactory-keycloak-themes-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-themes.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-themes-bootstrap/target/optionfactory-keycloak-themes-bootstrap-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-themes-bootstrap.jar \
		--mount type=bind,source=${PWD}/local/keycloak.conf,target=/opt/keycloak/conf/keycloak.conf \
		optionfactory/debian13-jdk25-keycloak2:213 --verbose


local-db: local-network
	docker run -d -ti --rm \
		--network keycloak \
		--name keycloak-psql \
		--ip 172.18.26.3 \
		--mount type=bind,source=${PWD}/local/00_init_db.sql,target=/sql-init.d/00_init_db.sql,readonly \
		--mount type=bind,source=${PWD}/local/pg_hba.conf,target=/var/lib/postgresql/conf/pg_hba.conf \
		--mount type=bind,source=${PWD}/local/postgres,target=/var/lib/postgresql/data \
		optionfactory/debian13-postgres17:213

local-ldap: local-network
	docker run -ti --rm \
		--network keycloak \
		--name keycloak-openldap \
		--ip 172.18.26.4 \
		--env LDAP_ADMIN_USERNAME=admin \
		--env LDAP_ADMIN_PASSWORD=admin \
		--env LDAP_USERS=user \
		--env LDAP_PASSWORDS=user \
		--env LDAP_ROOT=dc=notprod,dc=net \
		--env LDAP_ADMIN_DN=cn=admin,dc=notprod,dc=net \
		--env LDAP_GROUP=writers \
		bitnami/openldap:2.5.19

local-smtp: local-network
	docker run -d -it --rm \
		--network keycloak \
		--name keycloak-smtp \
		--ip 172.18.26.5 \
		maildev/maildev:2.2.1 --web 8080 --smtp 2525 --web-user admin --web-pass admin

update-local-initdb-script: 
	docker exec -ti keycloak-psql pg_dump --create -U postgres keycloak > local/00_init_db.sql


local-test-api:
	@echo "fetching token"
	$(eval TOKEN := $(shell curl --silent --data "grant_type=client_credentials&client_id=test-sa-client&client_secret=LokDtT7ZAwhEvekQcQLLW7c89YQIbkce" http://172.18.26.2:8080/realms/test/protocol/openid-connect/token))
	$(eval ACCESS_TOKEN := $(shell echo '${TOKEN}' | jq -r '.access_token'))
	@echo "inspecting users"
	@echo " filter id EQ"
	curl -v 'http://172.18.26.2:8080/admin/realms/test/inspection/users/?offset=0&limit=10' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}' \
		-H 'Content-Type: application/json' \
		--data '{"id": ["NEQ","CASE_SENSITIVE","dfef0e06-05d1-4103-a8a4-f3a19a7e2802"]}'
	@echo ""
	@echo ""
	@echo " filter id NEQ"
	curl -v 'http://172.18.26.2:8080/admin/realms/test/inspection/users/?offset=0&limit=10' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}' \
		-H 'Content-Type: application/json' \
		--data '{"id": ["EQ","CASE_SENSITIVE", "3d0bd8f9-ad4a-4d08-babb-14a48f210450"]}'
	@echo ""
	@echo ""
	@echo " filter attribute CONTAINS"
	curl -v 'http://172.18.26.2:8080/admin/realms/test/inspection/users/?offset=0&limit=10' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}' \
		-H 'Content-Type: application/json' \
		--data '{"attributes": ["CONTAINS","CASE_SENSITIVE","TEST_ATTRIBUTE","VALUE"]}'
	@echo ""
	@echo ""
	@echo " filter group ANY"
	curl -v 'http://172.18.26.2:8080/admin/realms/test/inspection/users/?offset=0&limit=10&sort=username,ASC' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}' \
		-H 'Content-Type: application/json' \
		--data '{"groups": ["ANY","test-group", "unknown-group", "readers"]}'
	@echo ""
	@echo ""
	@echo " exact user search by id"
	curl -v 'http://172.18.26.2:8080/admin/realms/test/inspection/users/3d0bd8f9-ad4a-4d08-babb-14a48f210450' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}'
	@echo ""
	@echo ""
	@echo " exact user search by username"
	curl -v 'http://172.18.26.2:8080/admin/realms/test/inspection/users/?username=user@example.com' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}'
	@echo ""
	@echo ""
	@echo "inspecting group membership"
	@echo ""
	curl -v 'http://172.18.26.2:8080/admin/realms/test/inspection/groups/membership' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}' \
		-H 'Content-Type: application/json' \
		--data '{}'
	@echo "inspecting group membership"
	@echo ""
	curl -X POST -v 'http://172.18.26.2:8080/admin/realms/test/inspection/groups/membership/LOCAL/TEST_GROUP' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}' \
		-H 'Content-Type: application/json' \
		--data '{}'		
	@echo ""
	@echo ""
	@echo "inspecting groups"
	@echo ""
	curl -X POST -v 'http://172.18.26.2:8080/admin/realms/test/inspection/groups' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}'
	@echo ""
	@echo ""
	@echo " provisioning: user patch: groups "
	curl -X PATCH -v 'http://172.18.26.2:8080/admin/realms/test/provisioning/users/' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}' \
		-H 'Content-Type: application/json' \
		--data '{"id":"3d0bd8f9-ad4a-4d08-babb-14a48f210450","attributesPatchMode":"APPEND","attributes":{"test": ["value1", "value2"]}, "groupsPatchMode":"APPEND", "groups": ["LOCAL/TEST_GROUP"]}'
	@echo " provide group"
	curl -X PUT -v 'http://172.18.26.2:8080/admin/realms/test/provisioning/groups/local/test/asd' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}'
	@echo ""
	@echo " delete group"
	curl -X DELETE -v 'http://172.18.26.2:8080/admin/realms/test/provisioning/groups/local/test/asd' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}'

