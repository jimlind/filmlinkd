provider "google" {
  project = "filmlinkd"
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_compute_instance" "bot-instance" {
  name         = "bot"
  machine_type = "e2-micro"
  zone         = "us-central1-a"

  labels = {
    container-vm = "filmlinkd-bot"
    name         = "filmlinkd-bot"
  }


  boot_disk {
    initialize_params {
      image = "cos-cloud/cos-stable"
      type  = "pd-standard"
      size  = 20
    }
  }

  # Something is required to load containers.
  # This is not an official API and subject to change at any time.
  # We shouldn't trust it, so the cloudbuild job repeats the process.
  metadata = {
    google-monitoring-enabled = "true"
    gce-container-declaration = <<-EOF
      spec:
        containers:
          - name: filmlinkd
            image: "ghcr.io/jimlind/filmlinkd/bot-image:latest"
        restartPolicy: Always
    EOF
    startup-script            = <<-EOF
      #!/bin/bash
      curl --ipv4 -vk -sSO https://dl.google.com/cloudagents/add-google-cloud-ops-agent-repo.sh
      bash add-google-cloud-ops-agent-repo.sh --also-install --version=latest
      sudo tee /etc/google-cloud-ops-agent/config.yaml > /dev/null << EOS
${file("${path.module}/ops-agent-config.yaml")}
EOS
      sudo systemctl restart google-cloud-ops-agent
    EOF
  }

  service_account {
    email = "compute-runner@filmlinkd.iam.gserviceaccount.com"
    scopes = ["https://www.googleapis.com/auth/cloud-platform"]
  }

  network_interface {
    subnetwork = var.subnet_self_link
  }
}

resource "google_compute_instance" "scraper-instance" {
  name         = "scraper"
  machine_type = "e2-micro"
  zone         = "us-central1-a"

  labels = {
    container-vm = "filmlinkd-scraper"
    name         = "filmlinkd-scraper"
  }


  boot_disk {
    initialize_params {
      image = "cos-cloud/cos-stable"
      type  = "pd-standard"
      size  = 20
    }
  }

  # Something is required to load containers.
  # This is not an official API and subject to change at any time.
  # We shouldn't trust it, so the cloudbuild job repeats the process.
  metadata = {
    google-monitoring-enabled = "true"
    gce-container-declaration = <<-EOF
      spec:
        containers:
          - name: filmlinkd
            image: "ghcr.io/jimlind/filmlinkd/scraper-image:latest"
        restartPolicy: Always
    EOF
    startup-script            = <<-EOF
      #!/bin/bash
      set -x # Keep this for command tracing

      # Define download URL and local path
      OPS_AGENT_DOWNLOAD_URL="https://dl.google.com/cloudagents/add-google-cloud-ops-agent-repo.sh"
      TEMP_SCRIPT_PATH="/tmp/add-google-cloud-ops-agent-repo.sh"
      FINAL_SCRIPT_DIR="/var/lib/google/scripts" # Writable and executable location on COS
      FINAL_SCRIPT_PATH="${FINAL_SCRIPT_DIR}/add-google-cloud-ops-agent-repo.sh"
      CURL_LOG_FILE="/tmp/curl_debug_output.log" # Log file for curl

      echo "Attempting to download Ops Agent script from $${OPS_AGENT_DOWNLOAD_URL} to $${TEMP_SCRIPT_PATH}" | tee -a "$${CURL_LOG_FILE}"

      # Step 1: Download the Ops Agent installation script to /tmp, forcing IPv4
      curl --ipv4 -k "$${OPS_AGENT_DOWNLOAD_URL}" -o "$${TEMP_SCRIPT_PATH}" 2>&1 | tee -a "$${CURL_LOG_FILE}"
      CURL_EXIT_CODE=$? # Capture curl's exit code immediately

      echo "Curl command finished with exit code: $${CURL_EXIT_CODE}" | tee -a "$${CURL_LOG_FILE}"

      # Check if the download was successful
      if [ "$${CURL_EXIT_CODE}" -ne 0 ]; then
          echo "Error: Failed to download $${OPS_AGENT_DOWNLOAD_URL} using IPv4. Curl exit code: $${CURL_EXIT_CODE}" >&2 | tee -a "$${CURL_LOG_FILE}"
          exit 1
      fi

      echo "Curl download reported success (exit code 0). Continuing..." | tee -a "$${CURL_LOG_FILE}"

      # Step 2: Ensure the target directory exists and copy the script there
      sudo mkdir -p "$${FINAL_SCRIPT_DIR}"

      # Check if mkdir was successful
      if [ $? -ne 0 ]; then
          echo "Error: Failed to create $${FINAL_SCRIPT_DIR}." >&2 | tee -a "$${CURL_LOG_FILE}"
          exit 7 # New error code for mkdir failure
      fi

      if [ ! -f "$${TEMP_SCRIPT_PATH}" ]; then
          echo "Error: Downloaded script $${TEMP_SCRIPT_PATH} does not exist after curl." >&2 | tee -a "$${CURL_LOG_FILE}"
          exit 6
      fi

      sudo cp "$${TEMP_SCRIPT_PATH}" "$${FINAL_SCRIPT_PATH}"

      # Check if copy was successful
      if [ $? -ne 0 ]; then
          echo "Error: Failed to copy script from $${TEMP_SCRIPT_PATH} to $${FINAL_SCRIPT_PATH}." >&2 | tee -a "$${CURL_LOG_FILE}"
          exit 8 # New error code for copy failure
      fi

      # Step 3: Make the script executable in its final location
      sudo chmod +x "$${FINAL_SCRIPT_PATH}"

      # Check if chmod was successful
      if [ $? -ne 0 ]; then
          echo "Error: Failed to make $${FINAL_SCRIPT_PATH} executable." >&2 | tee -a "$${CURL_LOG_FILE}"
          exit 5
      fi

      echo "Script copied to $${FINAL_SCRIPT_PATH} and made executable. Running installation script..." | tee -a "$${CURL_LOG_FILE}"

      # Step 4: Run the installation script from its final location
      sudo "$${FINAL_SCRIPT_PATH}" --also-install --version=latest

      # Check if the installation script ran successfully
      if [ $? -ne 0 ]; then
          echo "Error: Ops Agent installation script failed. Exit code: $$?" >&2 | tee -a "$${CURL_LOG_FILE}"
          exit 2
      fi

      echo "Installation script completed. Creating config directory..." | tee -a "$${CURL_LOG_FILE}"

      # Step 5: Create the Ops Agent configuration directory if it doesn't exist
      sudo mkdir -p /etc/google-cloud-ops-agent/

      echo "Config directory created. Writing config file..." | tee -a "$${CURL_LOG_FILE}"

      # Step 6: Write the configuration file
      sudo tee /etc/google-cloud-ops-agent/config.yaml > /dev/null << EOT
${file("${path.module}/ops-agent-config.yaml")}
EOT

      # Check if the config file was written successfully
      if [ $? -ne 0 ]; then
          echo "Error: Failed to write Ops Agent config file. Exit code: $$?" >&2 | tee -a "$${CURL_LOG_FILE}"
          exit 3
      fi

      echo "Config file written. Restarting service..." | tee -a "$${CURL_LOG_FILE}"

      # Step 7: Restart the Ops Agent service (it should now exist)
      sleep 5 # Give systemd time to register the service
      sudo systemctl restart google-cloud-ops-agent

      # Check if the service restarted successfully
      if [ $? -ne 0 ]; then
          echo "Error: Failed to restart Ops Agent service. Exit code: $$?" >&2 | tee -a "$${CURL_LOG_FILE}"
          exit 4
      fi

      echo "Ops Agent installation and configuration script completed successfully." | tee -a "$${CURL_LOG_FILE}"
    EOF
  }

  service_account {
    email = "compute-runner@filmlinkd.iam.gserviceaccount.com"
    scopes = ["https://www.googleapis.com/auth/cloud-platform"]
  }

  network_interface {
    subnetwork = var.subnet_self_link
  }
}