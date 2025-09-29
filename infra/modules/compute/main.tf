provider "google" {
  project = "filmlinkd"
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_compute_instance" "bot-vm" {
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

  metadata = {
    google-monitoring-enabled = "true"
  }
  metadata_startup_script = templatefile("${path.module}/startup-bot.sh.tpl", {
    environment = var.environment
  })

  service_account {
    email  = "compute-runner@filmlinkd.iam.gserviceaccount.com"
    scopes = ["https://www.googleapis.com/auth/cloud-platform"]
  }

  network_interface {
    subnetwork = var.subnet_self_link
  }
}

resource "google_compute_instance" "scraper-vm" {
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

  metadata = {
    google-monitoring-enabled = "true"
  }
  metadata_startup_script = templatefile("${path.module}/startup-scraper.sh.tpl", {
    environment = var.environment
  })

  service_account {
    email  = "compute-runner@filmlinkd.iam.gserviceaccount.com"
    scopes = ["https://www.googleapis.com/auth/cloud-platform"]
  }

  network_interface {
    subnetwork = var.subnet_self_link
  }
}
