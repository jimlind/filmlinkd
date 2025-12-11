#!/bin/bash
set -euxo pipefail

echo "Booting up VM..."
echo "Hostname: $(hostname)"
echo "Date: $(date)"
echo "Environment: ${environment}"
echo "Timestamp: ${timestamp}"

# Set the Environment Variable
export FILMLINKD_ENVIRONMENT=${environment}

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

# Get Most Recent Filmlinkd Bot
BOT_URL=https://github.com/jimlind/filmlinkd/releases/latest/download/${app}.jar
mkdir -p /opt/filmlinkd
curl -fsSL "$BOT_URL" -o "/opt/filmlinkd/app.jar"

# Create Systemd Unit File
cat <<EOF | tee /etc/systemd/system/filmlinkd.service
[Unit]
Description=Filmlinkd
After=network.target

[Service]
WorkingDirectory=/opt/filmlinkd
ExecStart=/opt/java/bin/java -XX:+UseSerialGC -Xmx${max_heap_size}m -jar /opt/filmlinkd/app.jar
Restart=always
RestartSec=60

[Install]
WantedBy=default.target
EOF

# Enable Service and Run the Bot
systemctl daemon-reload
systemctl enable filmlinkd
systemctl start filmlinkd
