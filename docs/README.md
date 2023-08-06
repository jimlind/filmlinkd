# Documentation

## Run Locally (Using Local Node)

This one doesn't currently (July 2023) save to the database but that isn't a big deal really. Since
I mostly use this for local development so that's generally okay.

`> npm start --mode=solo`

### Run the different development bots

`> npm start --mode=sharded`
`> npm start --mode=scraper`
`> npm start --mode=vip`

### Run the different production bots

`> npm start --live --mode=sharded`
`> npm start --live --mode=scraper`
`> npm start --live --mode=vip`

## Run Locally (Using Docker Build)

`> pack build --builder=gcr.io/buildpacks/builder:google-22 filmlinkd-app`

`> docker run -it filmlinkd-app --env "--mode=sharded"`
`> docker run -it filmlinkd-app --env "--mode=scraper"`
`> docker run -it filmlinkd-app --env "--mode=vip"`

`> docker run -it filmlinkd-app --env "--live" --env "--mode=sharded"`
`> docker run -it filmlinkd-app --env "--live" --env "--mode=scraper"`
`> docker run -it filmlinkd-app --env "--live" --env "--mode=vip"`

## Running in Production

I have a Trigger configured in [Cloud Build](https://console.cloud.google.com/cloud-build/) that runs the [cloudbuild.yaml](https://github.com/jimlind/filmlinkd/blob/main/cloudbuild.yaml) action. The intent is for it to be run manually. It builds containers that it writes to [Artifacts Registry](https://console.cloud.google.com/artifacts) and automatically updates (and subsequently restarts) the neccessary [Compute Engines](https://console.cloud.google.com/compute/) used by the service.

There are 2 issues in GitHub ([issue](https://github.com/buildpacks/pack/issues/1579) and [issue](https://github.com/GoogleContainerTools/skaffold/issues/7146)) that can potentially be a big change for the Buildpack that is used by the build process. Until these are resolved the `gcr.io/k8s-skaffold/pack` continues to be the best container for buildpack actions.

The containers are written to the [Container Registry](https://console.cloud.google.com/gcr/) (that is
getting replaced with Artifact Registry eventually). The URLs for for the generated containers can be
used in [Compute Engine](https://console.cloud.google.com/compute/) with no additional effort.

I have 2x e2-micro instances each running `scraper` and `vip`.
I have 1x e2-medium instance running `sharded`

## Production Networking

Each Compute Engine instance defaults to having a static IP address so that it has the easiest direct
connection to the Internet. But static IP addresses cost extra money so it is more economical to
disable that static IP address and have a single network
[VPC Network](https://console.cloud.google.com/networking/) that has a
[Cloud Nat](https://console.cloud.google.com/net-services/nat/) attached as the interface to the public
Internet.

## Deploying Changes to Production

The final steps to getting the application in production are completely manual. Copy the URL for the
container from the Container Registry and edit the Compute Engine instance and paste the URL into the Container Image box. When you save it looks like it automatically resets the instance but it doesn't actually. Hit the button to reset the instance.

## Pub/Sub Message Systems

### Log Entry

There are one or more scrapers that are not tied to specific servers that publish messages to
Google's Pub/Sub that are picked up the bot shards that can then send to the servers they are
attached to.

They are published on the "filmlinkd-log-entry-topic" topic.
They are recieved on the "filmlinkd-log-entry-subscription" subscriptions.

We should ack these messages immediatly as there is a general assumption that they'll be repeated
with some frequency.

### Command

Sometimes we want other parts of the system to know that a command was issued. Right now I only know that the `/follow` command needs to be wide-spread but maybe something else will get there.

They are published on the "filmlinkd-command-topic" topic.
They are recieved on the "filmlinkd-command-subscription" subscription.
