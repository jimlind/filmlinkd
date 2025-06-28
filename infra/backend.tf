terraform {
  backend "gcs" {
    bucket = "filmlinkd-state"
    prefix = "infra/state"
  }
}