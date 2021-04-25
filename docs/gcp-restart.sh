#!/bin/bash

# Change to the app directory
cd /opt/app/filmlinkd/

# Stop the service
sudo systemctl stop filmlinkd

# Delete all existing node modules
rm -rf node_modules

# Fresh pull from git
git pull

# Fresh install all node modles
npm install --omit=dev

# Start the service
sudo systemctl stop filmlinkd