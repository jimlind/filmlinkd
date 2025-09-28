#!/bin/bash
CONTAINER_NAME="filmlinkd-bot"

echo "Clearing old VM containers..."
docker stop $CONTAINER_NAME || true
docker rm $CONTAINER_NAME || true

echo "Booting up VM..."
echo "Hostname: $(hostname)"
echo "Date: $(date)"

# Configure docker with credentials for gcr.io and pkg.dev
export HOME=/home/appuser
docker-credential-gcr configure-docker

# Run the bot container in development mode
docker run \
  --name=$CONTAINER_NAME \
  --restart=always \
  -e FILMLINKD_ENVIRONMENT=DEVELOPMENT \
  ghcr.io/jimlind/filmlinkd/bot-image:latest