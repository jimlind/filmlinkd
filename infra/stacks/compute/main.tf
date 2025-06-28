provider "google" {
  project = "849234463174"
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_compute_instance" "instances" {
  name         = "bot"
  machine_type = "e2-micro"
  zone         = "us-central1-a"

  labels = {
    container-vm = "filmlinkd-bot"
    name         = "filmlinkd-bot"
  }

  boot_disk {
    source = var.attached_bot_data_disk_self_link
  }

  network_interface {
    network = "default"
    access_config {}
  }
}