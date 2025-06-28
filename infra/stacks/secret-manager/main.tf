provider "google" {
  project = "849234463174"
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_secret_manager_secret" "discord_dev_bot_token" {
  project   = "888325130733"
  secret_id = "DISCORD_DEV_BOT_TOKEN"
  lifecycle {
    prevent_destroy = true
  }
}

resource "google_secret_manager_secret" "discord_prod_bot_token" {
  project   = "888325130733"
  secret_id = "DISCORD_PROD_BOT_TOKEN"
  lifecycle {
    prevent_destroy = true
  }
}

resource "google_secret_manager_secret" "letterboxd_api_key" {
  project   = "888325130733"
  secret_id = "LETTERBOXD_API_KEY"
  lifecycle {
    prevent_destroy = true
  }
}

resource "google_secret_manager_secret" "letterboxd_api_shared_secret" {
  project   = "888325130733"
  secret_id = "LETTERBOXD_API_SHARED_SECRET"
  lifecycle {
    prevent_destroy = true
  }
}