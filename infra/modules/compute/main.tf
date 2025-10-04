resource "google_compute_instance" "this" {
  name         = var.name
  machine_type = var.machine_type
  zone         = "us-central1-a"

  labels = {
    container-vm = var.label
    name         = var.label
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
    last_restart_timestamp    = timestamp()
  }
  metadata_startup_script = templatefile("${path.module}/${var.startup_script_template}", {
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
