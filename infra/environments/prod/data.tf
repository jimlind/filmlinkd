data "terraform_remote_state" "shared" {
  backend = "gcs"
  config = {
    bucket = "filmlinkd-state"
    prefix = "shared/state"
  }
}