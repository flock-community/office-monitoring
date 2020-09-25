# Use the official maven/Java 8 image to create a build artifact.
# https://hub.docker.com/_/maven
FROM maven:3.6.3-openjdk-11-slim as builder

WORKDIR app

COPY pom.xml /app
RUN mvn dependency:go-offline -B

COPY src ./app/src

# Build a release artifact.
RUN mvn verify -DskipTests


# Runner
FROM adoptopenjdk/openjdk14:alpine-jre

WORKDIR /app

COPY --from=builder app/target/*.jar sensor-ingestion.jar
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "sensor-ingestion.jar"]
