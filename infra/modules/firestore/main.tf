provider "google" {
  project = "filmlinkd"
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_firestore_database" "database" {
  project     = "filmlinkd"
  name        = "(default)"
  location_id = "us-central1"
  type        = "FIRESTORE_NATIVE"
  lifecycle {
    prevent_destroy = true
  }
}