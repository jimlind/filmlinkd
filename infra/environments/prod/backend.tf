terraform {
  backend "gcs" {
    bucket = "filmlinkd-state"
    prefix = "environments/prod/state"
  }
}
