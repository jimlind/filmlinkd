terraform {
  backend "gcs" {
    bucket = "my-opentofu-state"
    prefix = "compute/state"
  }
}