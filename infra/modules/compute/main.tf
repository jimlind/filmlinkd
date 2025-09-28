provider "google" {
  project = "filmlinkd"
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_compute_instance" "bot-instance" {
  name         = "bot"
  machine_type = "e2-micro"
  # I might be able to use "e2-custom-small-4096" as a machine type but there is no documentation
  # supporting this. Only a text block on the console.
  zone = "us-central1-a"

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
  }
  # This probably doesn't actually work. :(
  metadata_startup_script = "echo FILMLINKD_ENVIRONMENT=PRODUCTION >> /etc/profile"

  service_account {
    email  = "compute-runner@filmlinkd.iam.gserviceaccount.com"
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
  }
  # This probably doesn't actually work. :(
  metadata_startup_script = "echo FILMLINKD_ENVIRONMENT=PRODUCTION >> /etc/profile"

  service_account {
    email  = "compute-runner@filmlinkd.iam.gserviceaccount.com"
    scopes = ["https://www.googleapis.com/auth/cloud-platform"]
  }

  network_interface {
    subnetwork = var.subnet_self_link
  }
}
