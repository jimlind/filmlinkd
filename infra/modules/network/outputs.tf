output "network_self_link" {
  description = "The self_link of the network."
  value       = google_compute_network.compute_network_vpc.self_link
}

output "subnet_self_link" {
  description = "The self_link of the subnet."
  value       = google_compute_subnetwork.compute_subnetwork_vpc.self_link
}