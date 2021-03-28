#!make
#include .env
include frontend/.env
export $(shell sed 's/=.*//' frontend/.env)

DOCKER_TAG=flock/office-monitoring
DOCKER_TAG_BE=${DOCKER_TAG}-be:latest
DOCKER_TAG_UI=${DOCKER_TAG}-ui:latest

CONTAINER_NAME=flock-office-monitoring
CONTAINER_NAME_BE=${CONTAINER_NAME}-be
CONTAINER_NAME_UI=${CONTAINER_NAME}-ui


be-build:
	docker build -t ${DOCKER_TAG_BE} . -f Dockerfile-be
.PHONY: be-build

be-run:
	docker run --name ${CONTAINER_NAME_BE} --rm -d -p8080:8080 ${DOCKER_TAG_BE}
.PHONY: be-run

be-log:
	docker logs -f ${CONTAINER_NAME_BE}
.PHONY: be-log

be-stop:
	docker stop ${CONTAINER_NAME_BE}
.PHONY: be-stop

be-publish:
	echo "implement docker push"
.PHONY: be-publish

be-destroy:
	docker rmi ${DOCKER_TAG_BE}
.PHONY: be-destroy

fe-build:
	docker build -t ${DOCKER_TAG_UI} --build-arg _HOST frontend
.PHONY: fe-build

fe-run:
	docker run --name ${CONTAINER_NAME_UI} --rm -d -p3000:80 ${DOCKER_TAG_UI}
.PHONY: fe-run

fe-log:
	docker logs -f ${CONTAINER_NAME_UI}
.PHONY: fe-log

fe-stop:
	docker stop ${CONTAINER_NAME_UI}
.PHONY: fe-stop

fe-publish:
	echo "implement docker push"
.PHONY: fe-publish

fe-destroy:
	docker rmi ${DOCKER_TAG_UI}
.PHONY: fe-destroy
