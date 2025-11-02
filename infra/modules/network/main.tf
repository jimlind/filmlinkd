resource "google_compute_network" "compute_network_vpc" {
  name                    = "compute-network-vpc"
  project                 = "filmlinkd"
  auto_create_subnetworks = false

  enable_ula_internal_ipv6                  = false
  network_firewall_policy_enforcement_order = "AFTER_CLASSIC_FIREWALL"
  description                               = ""
  delete_default_routes_on_create           = false
  network_profile                           = ""

  lifecycle {
    prevent_destroy = true
  }
}

resource "google_compute_subnetwork" "compute_subnetwork_vpc" {
  name          = "compute-subnetwork-vpc"
  ip_cidr_range = "10.0.0.0/24"
  region        = "us-central1"
  network       = google_compute_network.compute_network_vpc.self_link

  description             = ""
  ipv6_access_type        = ""
  reserved_internal_range = ""
  role                    = ""

  lifecycle {
    prevent_destroy = true
  }
}
