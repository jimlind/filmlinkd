#!/bin/bash

###
### Install the Cloud Logging Agent
###
logging_agent_install_file="/tmp/google-fluentd-install.sh" 
curl https://storage.googleapis.com/signals-agents/logging/google-fluentd-install.sh -o $logging_agent_install_file
bash $logging_agent_install_file
#rm /tmp/google-fluentd-install.sh
service google-fluentd start
service google-fluentd restart

###
### Install the Compute Engine Monitoring Agent
###
monitoring_agent_install_file="/tmp/add-monitoring-agent-repo.sh"
curl https://dl.google.com/cloudagents/add-monitoring-agent-repo.sh -o $monitoring_agent_install_file
bash $monitoring_agent_install_file
#rm /tmp/add-monitoring-agent-repo.sh
apt-get update
apt-get install stackdriver-agent
service stackdriver-agent start
service stackdriver-agent restart
