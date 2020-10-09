# MQTT to QUEUE Connector

Publishes all messages from the MQTT broker to a GCP pub/sub topic

##GCP Credentials

In order to connect to GCP there needs to be a `GOOGLE_APPLICATION_CREDENTIALS` environment variable with the location of the service account credentials json

## Deploy

To deploy a new docker image on the Raspberry execute `./deploy-on-raspberry.sh` from the `mqtt-to-queue-connector` directory