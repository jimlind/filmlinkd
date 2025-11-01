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

  point_in_time_recovery_enablement = "POINT_IN_TIME_RECOVERY_DISABLED"
  deletion_policy                   = "ABANDON"

  lifecycle {
    prevent_destroy = true
  }
}
