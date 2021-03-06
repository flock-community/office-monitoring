Ideeen

# Running the back-end service
- Run the command `mvn clean install`
- Login into Google Cloud using the command `gcloud auth application-default login`
- Make sure the credentials JSON file is saved on your local environment, i.e.: `~/.config/gcloud/application_default_credentials.json`
- Run the main class in: `flock.community.office.monitoring.backend.OfficeBackendApplication`

# Infrastructuur
- GCP account
- Broker in de cloud

- Sensor query current state
- Pairing 
    - Niet default pairing, 
    - Sensor id registratie. Iedere sensor wordt op een apart sub topic geregistreerd 
- Commands 
    - Hebben we hier de broker voor nodig?
- Queue tussen broker en applicatie
   
# Backend
- CI/CD pipeline
- Opslaan van events - ttl
- Streaming api
- Flock eco
- Graphql simple bindings voor dtos

# Dashboard
- CI/CD
- React 
- Streaming met RSocket
- React hooks voor state
- Grafiek met historie

# Regen detectie
- Buienradar/Buienalarm registratie


## Create / update indexes for google data store

To ensure efficient querying, we added composite indexes to the data store in gcp

```bash
$ gcloud datastore indexes create office-backend/index.yaml
```