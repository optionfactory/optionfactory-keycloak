BRIGE_ADDRESS=172.18.26.1
BRIGE_SUBNET=172.18.26.0/24

build:
	mvn clean package
bump:
	mvn versions:set -DgenerateBackupPoms=false
deploy-ossrh:
	mvn clean deploy -Possrh

local-network: 
	-@docker network create -o "com.docker.network.bridge.name"="keycloak" --subnet ${BRIGE_SUBNET} -d bridge keycloak

local-keycloak: local-network
	$(eval CURRENT_VERSION := $(shell mvn help:evaluate -Dexpression=project.version -q -DforceStdout))
	docker run -ti --rm \
		--network keycloak \
		--name keycloak \
		-p ${BRIGE_ADDRESS}:8081:8080 \
		-e KEYCLOAK_ADMIN=admin \
		-e KEYCLOAK_ADMIN_PASSWORD=admin \
		-v${PWD}/optionfactory-keycloak-validation/target/optionfactory-keycloak-validation-${CURRENT_VERSION}.jar:/opt/keycloak/providers/optionfactory-keycloak-validation.jar \
		-v${PWD}/optionfactory-keycloak-resources-auth/target/optionfactory-keycloak-resources-auth-${CURRENT_VERSION}.jar:/opt/keycloak/providers/optionfactory-keycloak-resources-auth.jar \
		-v${PWD}/optionfactory-keycloak-email-sender/target/optionfactory-keycloak-email-sender-${CURRENT_VERSION}.jar:/opt/keycloak/providers/optionfactory-keycloak-email-sender.jar \
		-v${PWD}/optionfactory-keycloak-login-stats/target/optionfactory-keycloak-login-stats-${CURRENT_VERSION}.jar:/opt/keycloak/providers/optionfactory-keycloak-login-stats.jar \
		-v${PWD}/optionfactory-keycloak-provisioning-api/target/optionfactory-keycloak-provisioning-api-${CURRENT_VERSION}.jar:/opt/keycloak/providers/optionfactory-keycloak-provisioning-api.jar \
		-v${PWD}/optionfactory-keycloak-welcome/target/optionfactory-keycloak-welcome-${CURRENT_VERSION}.jar:/opt/keycloak/providers/optionfactory-keycloak-welcome.jar \
		-v${PWD}/local/keycloak.conf:/opt/keycloak/conf/keycloak.conf \
		optionfactory/ubuntu22-jdk17-quarkus-keycloak1


local-db: local-network
	docker run -d -ti --rm \
		--network keycloak \
		--name keycloak-psql \
		-p ${BRIGE_ADDRESS}:5432:5432 \
		-v ${PWD}/local/postgres-init:/sql-init.d/:ro \
		-v ${PWD}/local/postgres:/var/lib/postgresql/data \
		optionfactory/ubuntu22-postgres14

local-smtp: local-network
	docker run -d -it --rm \
		--network keycloak \
		--name keycloak-smtp \
		-p ${BRIGE_ADDRESS}:2525:2525 \
		-p ${BRIGE_ADDRESS}:8082:8082 \
		maildev/maildev:2.0.5 --web 8080 --smtp 2525 --web-user admin --web-pass admin

local-test-api:
	$(eval TOKEN := $(shell curl --data "username=admin&password=admin&grant_type=password&client_id=admin-cli" http://172.18.26.1:8081/realms/master/protocol/openid-connect/token))
	$(eval ACCESS_TOKEN := $(shell echo '${TOKEN}' | jq -r '.access_token'))
	@echo "Verifying validation"
	curl -v 'http://${BRIGE_ADDRESS}:8081/realms/master/provisioning/users/?offset=1&limit=1' \
		-H 'Authorization: bearer ${ACCESS_TOKEN}' \
		-H 'Content-Type: application/json' \
		--data '{"groups":[]}'
	@echo ""
	@echo ""
	@echo ""
	@echo "sample call"
	curl -v 'http://${BRIGE_ADDRESS}:8081/realms/master/provisioning/users/' \
		-H 'Authorization: bearer ${ACCESS_TOKEN}' \
		-H 'Content-Type: application/json' \
		--data '{"id": {"op": "NEQ","value": "dfef0e06-05d1-4103-a8a4-f3a19a7e2802"}, "groups":[], "attributes":[]}'
	@echo ""
