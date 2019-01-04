all: build

build: build-jar build-docker

build-jar:
	mvn package

build-docker:
	docker build -t csms/coffee-society-accrual:latest .

clean:
	mvn clean

analyze:
	mvn clean org.jacoco:jacoco-maven-plugin:prepare-agent package org.jacoco:jacoco-maven-plugin:report sonar:sonar

run-jar:
	java -jar target/coffee-society-accrual*.jar

run-docker:
	docker run -d --name coffee-society-accrual csms/coffee-society-accrual

tag:
	docker tag csms/coffee-society-accrual csms/coffee-society-accrual:${TAG}

push-latest:
	docker push csms/coffee-society-accrual:latest

push-tag:
	docker push csms/coffee-society-accrual:${TAG}

docker-login:
	@docker login -u "${DOCKER_ID}" -p "${DOCKER_PASS}"
	
docker-run: run-docker

docker-remove:
	docker rm -f coffee-society-accrual

docker-logs:
	docker logs coffee-society-accrual
