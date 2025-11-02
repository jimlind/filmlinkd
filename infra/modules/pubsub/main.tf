provider "google" {
  project = "filmlinkd"
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_pubsub_topic" "x_filmlinkd_dev_command_topic" {
  name    = "x_filmlinkd-dev-command-topic"
  project = "filmlinkd"

  message_storage_policy {
    allowed_persistence_regions = []
  }

  lifecycle {
    prevent_destroy = true
  }
}
resource "google_pubsub_topic" "x_filmlinkd_dev_log_entry_result_topic" {
  name    = "x_filmlinkd-dev-log-entry-result-topic"
  project = "filmlinkd"

  message_storage_policy {
    allowed_persistence_regions = []
  }

  lifecycle {
    prevent_destroy = true
  }
}
resource "google_pubsub_topic" "x_filmlinkd_dev_log_entry_topic" {
  name    = "x_filmlinkd-dev-log-entry-topic"
  project = "filmlinkd"

  message_storage_policy {
    allowed_persistence_regions = []
  }

  lifecycle {
    prevent_destroy = true
  }
}

resource "google_pubsub_topic" "x_filmlinkd_command_topic" {
  name    = "x_filmlinkd-command-topic"
  project = "filmlinkd"

  message_storage_policy {
    allowed_persistence_regions = []
  }

  lifecycle {
    prevent_destroy = true
  }
}
resource "google_pubsub_topic" "x_filmlinkd_log_entry_result_topic" {
  name    = "x_filmlinkd-log-entry-result-topic"
  project = "filmlinkd"

  message_storage_policy {
    allowed_persistence_regions = []
  }

  lifecycle {
    prevent_destroy = true
  }
}
resource "google_pubsub_topic" "x_filmlinkd_log_entry_topic" {
  name    = "x_filmlinkd-log-entry-topic"
  project = "filmlinkd"

  message_storage_policy {
    allowed_persistence_regions = []
  }

  lifecycle {
    prevent_destroy = true
  }
}


