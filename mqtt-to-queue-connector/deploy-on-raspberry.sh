./mvnw clean verify
scp Dockerfile pi@192.168.1.84:mqtt-to-queue-connector
scp target/mqtt-to-queue-connector*.jar pi@192.168.1.84:mqtt-to-queue-connector
ssh pi@192.168.1.84 "cd mqtt-to-queue-connector && docker build . -t mqtt-to-queue-connector:latest && docker restart mqtt-to-queue-connector"

echo "New docker image installed and started!"