terraform {
  backend "gcs" {
    bucket = "filmlinkd-state"
    prefix = "secret-manager/state"
  }
}