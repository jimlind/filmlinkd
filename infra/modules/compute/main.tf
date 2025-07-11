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
      curl -sSO https://dl.google.com/cloudagents/add-google-cloud-ops-agent-repo.sh
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
      # Step 1: Download the Ops Agent installation script to /tmp
      curl -sSO https://dl.google.com/cloudagents/add-google-cloud-ops-agent-repo.sh -o /tmp/add-google-cloud-ops-agent-repo.sh
      # Check if the download was successful
      if [ $? -ne 0 ]; then
          echo "Error: Failed to download add-google-cloud-ops-agent-repo.sh." >&2
          exit 1
      fi
      # Step 2: Make the script executable
      chmod +x /tmp/add-google-cloud-ops-agent-repo.sh
      # Step 3: Run the installation script
      sudo /tmp/add-google-cloud-ops-agent-repo.sh --also-install --version=latest
      # Check if the installation script ran successfully
      if [ $? -ne 0 ]; then
          echo "Error: Ops Agent installation script failed." >&2
          exit 2
      fi
      # Step 4: Create the Ops Agent configuration directory if it doesn't exist
      sudo mkdir -p /etc/google-cloud-ops-agent/
      # Step 5: Write the configuration file
      sudo tee /etc/google-cloud-ops-agent/config.yaml > /dev/null << EOT
${file("${path.module}/ops-agent-config.yaml")}
EOT
      # Check if the config file was written successfully
      if [ $? -ne 0 ]; then
          echo "Error: Failed to write Ops Agent config file." >&2
          exit 3
      fi
      # Step 6: Restart the Ops Agent service (it should now exist)
      # Adding a small delay to ensure the service unit is fully registered
      sleep 5
      sudo systemctl restart google-cloud-ops-agent
      # Check if the service restarted successfully
      if [ $? -ne 0 ]; then
          echo "Error: Failed to restart Ops Agent service." >&2
          exit 4
      fi
      echo "Ops Agent installation and configuration script completed successfully."
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