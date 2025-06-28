terraform {
  backend "gcs" {
    bucket = "filmlinkd-state"
    prefix = "infra-root/state"
  }
}