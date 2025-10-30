terraform {
  backend "gcs" {
    bucket = "filmlinkd-state"
    prefix = "shared/state"
  }
}
