#!/bin/bash

# Change to the app directory
cd /opt/app/filmlinkd/

# Stop the service
systemctl stop filmlinkd
sleep 2

# Delete all existing node modules and compiled files
rm -rf node_modules
rm -rf compiled

# Fresh pull from git
git pull

# Fresh install all node modles
npm install --omit=dev

# Start the service
systemctl start filmlinkd
sleep 2

# Output the service status
systemctl status filmlinkd