provider "google" {
  project = "filmlinkd"
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_secret_manager_secret" "test_discord_dev_bot_token" {
  project   = "filmlinkd"
  secret_id = "TEST_DISCORD_DEV_BOT_TOKEN"
  replication {
    auto {}
  }
  deletion_protection = false
  lifecycle {
    prevent_destroy = true
  }
}

resource "google_secret_manager_secret" "test_discord_prod_bot_token" {
  project   = "filmlinkd"
  secret_id = "TEST_DISCORD_PROD_BOT_TOKEN"
  replication {
    auto {}
  }
  deletion_protection = false
  lifecycle {
    prevent_destroy = true
  }
}

resource "google_secret_manager_secret" "test_letterboxd_api_key" {
  project   = "filmlinkd"
  secret_id = "TEST_LETTERBOXD_API_KEY"
  replication {
    auto {}
  }
  deletion_protection = false
  lifecycle {
    prevent_destroy = true
  }
}

resource "google_secret_manager_secret" "test_letterboxd_api_shared_secret" {
  project   = "filmlinkd"
  secret_id = "TEST_LETTERBOXD_API_SHARED_SECRET"
  replication {
    auto {}
  }
  deletion_protection = false
  lifecycle {
    prevent_destroy = true
  }
}
