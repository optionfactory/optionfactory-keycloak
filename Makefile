BRIDGE_SUBNET=172.18.26.0/24

build:
	mvn clean package
bump:
	mvn versions:set -DgenerateBackupPoms=false
deploy-ossrh:
	mvn clean deploy -Possrh
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
		-e DEBUG_PORT="*:8787" \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-providers/target/optionfactory-keycloak-providers-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-providers.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-remoting/target/optionfactory-keycloak-remoting-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-remoting.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-email-sender/target/optionfactory-keycloak-email-sender-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-email-sender.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-login-stats/target/optionfactory-keycloak-login-stats-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-login-stats.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-api-provisioning/target/optionfactory-keycloak-api-provisioning-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-api-provisioning.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-welcome/target/optionfactory-keycloak-welcome-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-welcome.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-bootstrap/target/optionfactory-keycloak-bootstrap-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-bootstrap.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-idp-apple/target/optionfactory-keycloak-idp-apple-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-idp-apple.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-authenticators/target/optionfactory-keycloak-authenticators-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-authenticators.jar \
		--mount type=bind,source=${PWD}/optionfactory-keycloak-cookies/target/optionfactory-keycloak-cookies-${CURRENT_VERSION}.jar,target=/opt/keycloak/providers/optionfactory-keycloak-cookies.jar \
		--mount type=bind,source=${PWD}/local/keycloak.conf,target=/opt/keycloak/conf/keycloak.conf \
		optionfactory/debian12-jdk21-keycloak2:102


local-db: local-network
	docker run -d -ti --rm \
		--network keycloak \
		--name keycloak-psql \
		--ip 172.18.26.3 \
		--mount type=bind,source=${PWD}/local/00_init_db.sql,target=/sql-init.d/00_init_db.sql,readonly \
		--mount type=bind,source=${PWD}/local/pg_hba.conf,target=/var/lib/postgresql/conf/pg_hba.conf \
		--mount type=bind,source=${PWD}/local/postgres,target=/var/lib/postgresql/data \
		optionfactory/debian12-postgres17:102

local-smtp: local-network
	docker run -d -it --rm \
		--network keycloak \
		--name keycloak-smtp \
		--ip 172.18.26.4 \
		maildev/maildev:2.0.5 --web 8080 --smtp 2525 --web-user admin --web-pass admin

update-local-initdb-script: 
	docker exec -ti keycloak-psql pg_dump --create -U postgres keycloak > local/00_init_db.sql


local-test-api:
	@echo "fetching token"
	$(eval TOKEN := $(shell curl --silent --data "grant_type=client_credentials&client_id=test-sa-view-client&client_secret=LokDtT7ZAwhEvekQcQLLW7c89YQIbkce" http://172.18.26.2:8080/realms/test/protocol/openid-connect/token))
	$(eval ACCESS_TOKEN := $(shell echo '${TOKEN}' | jq -r '.access_token'))
	@echo "inspecting users"
	@echo " filter id EQ"
	curl -v 'http://172.18.26.2:8080/realms/test/inspection/users/?offset=0&limit=10' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}' \
		-H 'Content-Type: application/json' \
		--data '{"id": ["NEQ","CASE_SENSITIVE","dfef0e06-05d1-4103-a8a4-f3a19a7e2802"]}'
	@echo ""
	@echo ""
	@echo " filter id NEQ"
	curl -v 'http://172.18.26.2:8080/realms/test/inspection/users/?offset=0&limit=10' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}' \
		-H 'Content-Type: application/json' \
		--data '{"id": ["EQ","CASE_SENSITIVE", "3d0bd8f9-ad4a-4d08-babb-14a48f210450"]}'
	@echo ""
	@echo ""
	@echo " filter attribute CONTAINS"
	curl -v 'http://172.18.26.2:8080/realms/test/inspection/users/?offset=0&limit=10' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}' \
		-H 'Content-Type: application/json' \
		--data '{"attributes": ["CONTAINS","CASE_SENSITIVE","TEST_ATTRIBUTE","VALUE"]}'
	@echo ""
	@echo ""
	@echo " filter group ANY"
	curl -v 'http://172.18.26.2:8080/realms/test/inspection/users/?offset=0&limit=10&sort=username,ASC' \
		-H 'Authorization: Bearer ${ACCESS_TOKEN}' \
		-H 'Content-Type: application/json' \
		--data '{"groups": ["ANY","test-group", "unknown-group"]}'
	@echo ""
	@echo ""
