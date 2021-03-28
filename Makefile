be-build:
	docker build -t flock/office-service:latest . -f Dockerfile-be
.PHONY: be-build

be-run:
	docker run --name flock-office-service --rm -d -p8080:8080 flock/office-service:latest
.PHONY: be-run

be-log:
	docker logs -f flock-office-service
.PHONY: be-log

be-stop:
	docker stop flock-office-service
.PHONY: be-stop

be-publish:
	echo "implement docker push"
.PHONY: be-publish

be-destroy:
	docker rmi flock/office-service:latest
.PHONY: be-destroy

fe-build:
	docker build -t flock/office-ui:latest --build-arg _HOST frontend
.PHONY: fe-build

fe-run:
	docker run --name flock-office-ui --rm -d -p3000:80 flock/office-ui:latest
.PHONY: fe-run

fe-log:
	docker logs -f flock-office-ui
.PHONY: fe-log

fe-stop:
	docker stop flock-office-ui
.PHONY: fe-stop

fe-publish:
	echo "implement docker push"
.PHONY: fe-publish

fe-destroy:
	docker rmi flock/office-ui:latest
.PHONY: fe-destroy
