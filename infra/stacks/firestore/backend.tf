terraform {
  backend "gcs" {
    bucket = "filmlinkd-state"
    prefix = "firestore/state"
  }
}