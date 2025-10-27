resource "google_compute_project_default_network_tier" "project-tier" {
  project      = "filmlinkd"
  network_tier = "STANDARD"
}

module "network" {
  source = "../modules/network"
}

# Development Computer Modules
module "bot-development" {
  source           = "../modules/compute"
  environment      = "DEVELOPMENT"
  name             = "bot-development"
  app              = "bot"
  label            = "filmlinkd-bot-development"
  machine_type     = "e2-micro"
  max_heap_size    = "256"
  subnet_self_link = module.network.subnet_self_link
}

module "scraper-development" {
  source           = "../modules/compute"
  environment      = "DEVELOPMENT"
  name             = "scraper-development"
  app              = "scraper"
  label            = "filmlinkd-scraper-development"
  machine_type     = "e2-micro"
  max_heap_size    = "256"
  subnet_self_link = module.network.subnet_self_link
}

# Production Computer Modules
module "bot-production" {
  source           = "../modules/compute"
  environment      = "PRODUCTION"
  name             = "bot-production"
  app              = "bot"
  label            = "filmlinkd-bot"
  machine_type     = "e2-medium"
  max_heap_size    = "1024"
  subnet_self_link = module.network.subnet_self_link
}

module "scraper-production" {
  source           = "../modules/compute"
  environment      = "PRODUCTION"
  name             = "scraper-production"
  app              = "scraper"
  label            = "filmlinkd-scraper"
  machine_type     = "e2-medium"
  max_heap_size    = "1024"
  subnet_self_link = module.network.subnet_self_link
}

module "cloudnat" {
  source            = "../modules/cloudnat"
  network_self_link = module.network.network_self_link
}

