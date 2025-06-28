terraform {
  backend "gcs" {
    bucket = "my-opentofu-state"
    prefix = "disks/state"
  }
}