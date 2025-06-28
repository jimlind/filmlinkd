provider "google" {
  project = "filmlinkd"
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_compute_disk" "bot_data_disk" {
  name    = "bot-disk"
  type    = "pd-standard"
  zone    = "us-central1-a"
  size    = 20
  project = "filmlinkd"
  image   = "cos-cloud/cos-stable"
}

