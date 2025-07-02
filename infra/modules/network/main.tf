resource "google_compute_network" "compute_network_vpc" {
  name                    = "compute-network-vpc"
  auto_create_subnetworks = false
  lifecycle {
    prevent_destroy = true
  }
}

resource "google_compute_subnetwork" "compute_subnetwork_vpc" {
  name          = "compute-subnetwork-vpc"
  ip_cidr_range = "10.0.0.0/24"
  region        = "us-central1"
  network       = google_compute_network.compute_network_vpc.self_link
  lifecycle {
    prevent_destroy = true
  }
}