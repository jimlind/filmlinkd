resource "google_compute_router" "compute_router" {
  name    = "compute-route"
  region  = "us-central1"
  network = var.network_self_link
}

resource "google_compute_router_nat" "compute_router_nat" {
  name                               = "compute-router-nat"
  router                             = google_compute_router.compute_router.name
  region                             = "us-central1"
  nat_ip_allocate_option             = "AUTO_ONLY"
  source_subnetwork_ip_ranges_to_nat = "ALL_SUBNETWORKS_ALL_IP_RANGES"

  log_config {
    enable = true
    filter = "ERRORS_ONLY"
  }
}