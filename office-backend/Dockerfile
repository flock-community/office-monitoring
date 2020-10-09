# Use the official maven/Java 8 image to create a build artifact.
# https://hub.docker.com/_/maven
FROM maven:3.6.3-openjdk-11-slim as builder

WORKDIR app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

# Build a release artifact.
RUN mvn verify

RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# Runner
FROM adoptopenjdk/openjdk14:alpine-jre

VOLUME /tmp

ARG DEPENDENCY=/app/target/dependency
COPY --from=builder ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=builder ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=builder ${DEPENDENCY}/BOOT-INF/classes /app

COPY --from=builder /app/target/dependency /tmp
EXPOSE 8080

ENTRYPOINT ["java",  "-cp","app:app/lib/*","flock.community.office.monitoring.sensoringestion.SensorIngestionApplicationKt"]
