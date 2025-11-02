resource "google_compute_router" "compute_router" {
  name    = "compute-route"
  region  = "us-central1"
  network = var.network_self_link

  description                   = ""
  encrypted_interconnect_router = false
}

resource "google_compute_router_nat" "compute_router_nat" {
  name                               = "compute-router-nat"
  router                             = google_compute_router.compute_router.name
  region                             = "us-central1"
  nat_ip_allocate_option             = "AUTO_ONLY"
  source_subnetwork_ip_ranges_to_nat = "ALL_SUBNETWORKS_ALL_IP_RANGES"

  type                                 = "PUBLIC"
  udp_idle_timeout_sec                 = 30
  tcp_established_idle_timeout_sec     = 1200
  tcp_time_wait_timeout_sec            = 120
  icmp_idle_timeout_sec                = 30
  tcp_transitory_idle_timeout_sec      = 30
  source_subnetwork_ip_ranges_to_nat64 = null

  log_config {
    enable = true
    filter = "ERRORS_ONLY"
  }

  enable_dynamic_port_allocation = true
  min_ports_per_vm               = 64
  max_ports_per_vm               = 65536
}
