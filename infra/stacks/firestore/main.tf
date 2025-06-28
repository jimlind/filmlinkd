provider "google" {
  project = "849234463174"
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_firestore_database" "database" {
  project     = "opentofu-test-project"
  name        = "(default)"
  location_id = "us-central1"
  type        = "FIRESTORE_NATIVE"
}