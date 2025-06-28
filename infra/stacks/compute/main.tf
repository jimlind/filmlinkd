provider "google" {
  project = "filmlinkd"
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
    initialize_params {
      image = "cos-cloud/cos-stable"
      type  = "pd-standard"
      size  = 20
    }
  }

  # Something is required to load containers
  metadata = {
    gce-container-declaration = <<-EOF
      spec:
        containers:
          - name: filmlinkd
            image: "ghcr.io/jimlind/filmlinkd:latest"
        restartPolicy: Always
    EOF
  }

  network_interface {
    network = "default"
    access_config {}
  }
}