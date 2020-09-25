FROM adoptopenjdk/openjdk14:alpine-jre

WORKDIR /app
COPY target/*.jar sensor-ingestion.jar
EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "sensor-ingestion.jar"]
