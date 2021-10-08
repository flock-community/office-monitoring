


## Create / update indexes for google data store

To ensure efficient querying, we added composite indexes to the data store in gcp

```bash
$ gcloud datastore indexes create office-backend/index.yaml
```

## Setup google-cloud for the backend

```
# install google cloud cli tools
brew install google-cloud-sdk

# login to get credentials
gcloud auth login

# Set personal credentials as default application credentials
gcloud auth application-default login
```


