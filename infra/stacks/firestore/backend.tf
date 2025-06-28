terraform {
  backend "gcs" {
    bucket = "my-opentofu-state"
    prefix = "firestore/state"
  }
}