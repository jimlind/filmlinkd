# Documentation

## Running in Production

I have a Trigger configured in [Cloud Build](https://console.cloud.google.com/cloud-build/) that runs
the [cloudbuild.yaml](https://github.com/jimlind/filmlinkd/blob/main/cloudbuild.yaml) action. The intent is for it to be
run manually. It builds containers that it writes to [Artifacts Registry](https://console.cloud.google.com/artifacts)
and automatically updates (and subsequently restarts) the
neccessary [Compute Engines](https://console.cloud.google.com/compute/) used by the service.

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

## Pub/Sub Message Systems

### Log Entry

There are one or more scrapers that are not tied to specific servers that publish messages to
Google's Pub/Sub that are picked up the bot shards that can then send to the servers they are
attached to.

They are published on the "filmlinkd-log-entry-topic" topic.
They are received on the "filmlinkd-log-entry-subscription" subscriptions.

We should ack these messages immediately as there is a general assumption that they'll be repeated
with some frequency.

### Command

Sometimes we want other parts of the system to know that a command was issued. Right now I only know that the `/follow`
command needs to be wide-spread but maybe something else will get there.

They are published on the "filmlinkd-command-topic" topic.
They are received on the "filmlinkd-command-subscription" subscription.
