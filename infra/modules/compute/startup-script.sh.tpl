#!/bin/bash
set -e

echo "Booting up VM..."
echo "Hostname: $(hostname)"
echo "Date: $(date)"
echo "Environment: ${environment}"
echo "Timestamp: ${timestamp}"

# Set the Environment Variable
FILMLINKD_ENVIRONMENT=${environment}

# Install Google Cloud Ops
curl -sSO https://dl.google.com/cloudagents/add-google-cloud-ops-agent-repo.sh
bash add-google-cloud-ops-agent-repo.sh --also-install

# Ensure Recent Needed System Utilities
apt-get update -qq && apt-get install -y --no-install-recommends curl ca-certificates jq

# Get Most Recent Temurin JRE
RELEASE_JSON=$(curl -s https://api.adoptium.net/v3/assets/latest/21/hotspot)
RELEASE_URL=$(echo "$RELEASE_JSON" | jq -r '.[] | select(.binary.architecture == "x64" and .binary.os == "linux" and .binary.image_type == "jre") | .binary.package.link')

# Install Downloaded Termurin
curl -fsSL "$RELEASE_URL" -o temurin-jre.tar.gz
mkdir -p /opt/java
tar -xzf temurin-jre.tar.gz -C /opt/java --strip-components=1
export PATH="/opt/java/bin:$PATH"

# Get Most Recent Filmlinkd Bot
BOT_URL=https://github.com/jimlind/filmlinkd/releases/latest/download/${app}.jar
curl -fsSL "$BOT_URL" -o app.jar

# Run the Bot
exec java -XX:+UseSerialGC -Xmx${max_heap_size}m -jar app.jar
