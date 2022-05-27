
build:
	mvn clean package
bump:
	mvn versions:set -DgenerateBackupPoms=false
deploy-ossrh:
	JAVA_HOME=/usr/java/jdk-11.0.11+9/ mvn clean deploy -Possrh

local-network: 
	-@docker network create -o "com.docker.network.bridge.name"="keycloak" --subnet 172.18.26.0/24 -d bridge keycloak

local-keycloak: local-network
	docker run -ti --rm \
		--network keycloak \
		--name keycloak \
		-p 172.18.26.1:8081:8080 \
		-e KEYCLOAK_ADMIN=admin \
		-e KEYCLOAK_ADMIN_PASSWORD=admin \
		-v${PWD}/optionfactory-keycloak-email-sender/target/optionfactory-keycloak-email-sender-1.1-SNAPSHOT.jar:/opt/keycloak/providers/optionfactory-keycloak-email-sender.jar \
		-v${PWD}/optionfactory-keycloak-login-stats/target/optionfactory-keycloak-login-stats-1.1-SNAPSHOT.jar:/opt/keycloak/providers/optionfactory-keycloak-login-stats.jar \
		-v${PWD}/local/keycloak.conf:/opt/keycloak/conf/keycloak.conf \
		optionfactory/ubuntu22-jdk17-quarkus-keycloak1


local-db: local-network
	docker run -d -ti --rm \
		--network keycloak \
		--name keycloak-psql \
		-p 172.18.26.1:5432:5432 \
		-v ${PWD}/local/postgres-init:/sql-init.d/:ro \
		-v ${PWD}/local/postgres:/var/lib/postgresql/data \
		optionfactory/ubuntu22-postgres14

local-smtp: local-network
	docker run -d -it --rm \
		--network keycloak \
		--name keycloak-smtp \
		-p 172.18.26.1:2525:25 \
		-p 172.18.26.1:8082:80 \
		maildev/maildev:2.0.5 --web 80 --smtp 25 --web-user admin --web-pass admin
