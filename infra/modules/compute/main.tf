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
      image = "debian-cloud/debian-12"
      type  = "pd-standard"
      size  = 10
    }
  }

  metadata_startup_script = templatefile("${path.module}/startup-script.sh.tpl", {
    app           = var.app
    environment   = var.environment
    max_heap_size = var.max_heap_size
    timestamp     = formatdate("YYYYMMDD-HHmm", timestamp())
  })

  service_account {
    email  = "compute-runner@filmlinkd.iam.gserviceaccount.com"
    scopes = ["https://www.googleapis.com/auth/cloud-platform"]
  }

  network_interface {
    subnetwork = var.subnet_self_link
  }
}
