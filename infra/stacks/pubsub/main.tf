provider "google" {
  project = "filmlinkd"
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_pubsub_topic" "filmlinkd_dev_command_topic" {
  name    = "filmlinkd-dev-command-topic"
  project = "filmlinkd"
  lifecycle {
    prevent_destroy = true
  }
}
resource "google_pubsub_topic" "filmlinkd_dev_log_entry_result_topic" {
  name    = "filmlinkd-dev-log-entry-result-topic"
  project = "filmlinkd"
  lifecycle {
    prevent_destroy = true
  }
}
resource "google_pubsub_topic" "filmlinkd_dev_log_entry_topic" {
  name    = "filmlinkd-dev-log-entry-topic"
  project = "filmlinkd"
  lifecycle {
    prevent_destroy = true
  }
}

resource "google_pubsub_topic" "filmlinkd_command_topic" {
  name    = "filmlinkd-command-topic"
  project = "filmlinkd"
  lifecycle {
    prevent_destroy = true
  }
}
resource "google_pubsub_topic" "filmlinkd_log_entry_result_topic" {
  name    = "filmlinkd-log-entry-result-topic"
  project = "filmlinkd"
  lifecycle {
    prevent_destroy = true
  }
}
resource "google_pubsub_topic" "filmlinkd_log_entry_topic" {
  name    = "filmlinkd-log-entry-topic"
  project = "filmlinkd"
  lifecycle {
    prevent_destroy = true
  }
}


