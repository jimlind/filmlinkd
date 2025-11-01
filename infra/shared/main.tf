resource "google_compute_project_default_network_tier" "project-tier" {
  project      = "filmlinkd"
  network_tier = "STANDARD"
}

# module "network" {
#   source = "../modules/network"
# }

module "firestore" {
  source = "../modules/firestore"
}

module "pubsub" {
  source = "../modules/pubsub"
}

module "secret-manager" {
  source = "../modules/secret-manager"
}
