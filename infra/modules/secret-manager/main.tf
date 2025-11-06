provider "google" {
  project = "filmlinkd"
  region  = "us-central1"
  zone    = "us-central1-a"
}

resource "google_secret_manager_secret" "discord_dev_bot_token" {
  project   = "filmlinkd"
  secret_id = "DISCORD_DEV_BOT_TOKEN"
  replication {
    auto {}
  }

  annotations         = {}
  deletion_protection = false
  labels              = {}
  version_aliases     = {}
  version_destroy_ttl = ""

  lifecycle {
    prevent_destroy = true
  }
}

resource "google_secret_manager_secret" "discord_prod_bot_token" {
  project   = "filmlinkd"
  secret_id = "DISCORD_PROD_BOT_TOKEN"
  replication {
    auto {}
  }

  annotations         = {}
  deletion_protection = false
  labels              = {}
  version_aliases     = {}
  version_destroy_ttl = ""

  lifecycle {
    prevent_destroy = true
  }
}

resource "google_secret_manager_secret" "letterboxd_api_key" {
  project   = "filmlinkd"
  secret_id = "LETTERBOXD_API_KEY"
  replication {
    auto {}
  }

  annotations         = {}
  deletion_protection = false
  labels              = {}
  version_aliases     = {}
  version_destroy_ttl = ""

  lifecycle {
    prevent_destroy = true
  }
}

resource "google_secret_manager_secret" "letterboxd_api_shared_secret" {
  project   = "filmlinkd"
  secret_id = "LETTERBOXD_API_SHARED_SECRET"
  replication {
    auto {}
  }

  annotations         = {}
  deletion_protection = false
  labels              = {}
  version_aliases     = {}
  version_destroy_ttl = ""

  lifecycle {
    prevent_destroy = true
  }
}

resource "google_secret_manager_secret" "the_movie_database_api_key" {
  project   = "filmlinkd"
  secret_id = "THE_MOVIE_DATABASE_API_KEY"
  replication {
    auto {}
  }

  annotations         = {}
  deletion_protection = false
  labels              = {}
  version_aliases     = {}
  version_destroy_ttl = ""

  lifecycle {
    prevent_destroy = true
  }
}
