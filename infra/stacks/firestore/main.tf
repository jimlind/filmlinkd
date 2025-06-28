provider "google" {
  project = "filmlinkd"
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_firestore_database" "database" {
  project     = "filmlinkd"
  # The "name" should actually be "(default)" but the system doesn't want you to be doing this all the time so there is
  # a 30-day limit on creating after it was deleted. If it is "(default)" other things work better.
  name        = "filmlinkd-db"
  location_id = "us-central1"
  type        = "FIRESTORE_NATIVE"
  lifecycle {
    prevent_destroy = true
  }
}