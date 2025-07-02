terraform {
  backend "gcs" {
    bucket = "filmlinkd-state"
    prefix = "compute/state"
  }
}