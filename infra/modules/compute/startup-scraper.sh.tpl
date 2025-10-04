#!/bin/bash
CONTAINER_NAME="filmlinkd-scraper"

echo "Clearing old VM containers..."
docker stop $CONTAINER_NAME || true
docker rm $CONTAINER_NAME || true

echo "Booting up VM..."
echo "Hostname: $(hostname)"
echo "Date: $(date)"
echo "Environment: ${environment}"
echo "Timestamp: ${timestamp}"

# Configure docker with credentials for gcr.io and pkg.dev
export HOME=/home/appuser
docker-credential-gcr configure-docker

# Run the scraper container using the environment variable
docker pull ghcr.io/jimlind/filmlinkd/scraper-image:latest
docker run \
  --name=$CONTAINER_NAME \
  --restart=always \
  -e FILMLINKD_ENVIRONMENT=${environment} \
  ghcr.io/jimlind/filmlinkd/scraper-image:latest