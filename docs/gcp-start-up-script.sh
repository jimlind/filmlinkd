#!/bin/bash

###
### Install the Cloud Logging Agent
###
logging_agent_install_file="/tmp/google-fluentd-install.sh" 
curl -sSO https://dl.google.com/cloudagents/add-logging-agent-repo.sh -o $logging_agent_install_file
bash $logging_agent_install_file
#rm /tmp/google-fluentd-install.sh
apt-get update
apt-get install -y google-fluentd
apt-get install -y google-fluentd-catch-all-config
service google-fluentd start
service google-fluentd restart

###
### Install the Compute Engine Monitoring Agent
###
monitoring_agent_install_file="/tmp/add-monitoring-agent-repo.sh"
curl -sSO https://dl.google.com/cloudagents/add-monitoring-agent-repo.sh -o $monitoring_agent_install_file
bash $monitoring_agent_install_file
#rm /tmp/add-monitoring-agent-repo.sh
apt-get update
apt-get install -y stackdriver-agent
service stackdriver-agent start
service stackdriver-agent restart

# Example Google Install Scripts
#curl -sSO https://dl.google.com/cloudagents/add-logging-agent-repo.sh && sudo bash add-logging-agent-repo.sh && sudo apt-get update && sudo apt-get install google-fluentd && sudo apt-get install -y google-fluentd-catch-all-config && sudo service google-fluentd start"
#curl -sSO https://dl.google.com/cloudagents/add-monitoring-agent-repo.sh && sudo bash add-monitoring-agent-repo.sh && sudo apt-get update && sudo apt-get install stackdriver-agent && sudo service stackdriver-agent start"
