#!/bin/bash

###
### Install the Cloud Logging Agent
###
curl /tmp/google-fluentd-install.sh https://storage.googleapis.com/signals-agents/logging/google-fluentd-install.sh
bash /tmp/google-fluentd-install.sh
#rm /tmp/google-fluentd-install.sh
service google-fluentd start
service google-fluentd restart

###
### Install the Compute Engine Monitoring Agent
###
curl /tmp/add-monitoring-agent-repo.sh https://dl.google.com/cloudagents/add-monitoring-agent-repo.sh
bash /tmp/add-monitoring-agent-repo.sh
#rm /tmp/add-monitoring-agent-repo.sh
apt-get update
apt-get install stackdriver-agent
service stackdriver-agent start
service stackdriver-agent restart

