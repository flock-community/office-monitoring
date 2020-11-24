## Use the official maven/Java 11 image to create a build artifact.
## https://hub.docker.com/_/maven
#FROM maven:3.6.3-openjdk-11-slim as builder
#
#WORKDIR app
#
#COPY pom.xml .
#RUN mvn dependency:go-offline -B
#
#COPY src ./src
#
## Build a release artifact.
#RUN mvn verify
#
#RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)
#
## Runner
#FROM adoptopenjdk/openjdk14:alpine-jre
#
#ARG DEPENDENCY=/app/target/dependency
#COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
#COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
#COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app
#
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-cp", "app:app/lib/*", "flock.community.office.monitoring.backend.OfficeBackendApplicationKt"]
FROM openjdk:8-jdk-alpine
ARG JAR_FILE=JAR_FILE_MUST_BE_SPECIFIED_AS_BUILD_ARG
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-Djava.security.edg=file:/dev/./urandom","-jar","/app.jar"]