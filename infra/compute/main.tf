resource "google_compute_project_default_network_tier" "project-tier" {
  project      = "filmlinkd"
  network_tier = "STANDARD"
}

module "network" {
  source = "../modules/network"
}

# Development Computer Modules
module "bot-development" {
  source                  = "../modules/compute"
  environment             = "DEVELOPMENT"
  name                    = "bot-development"
  label                   = "filmlinkd-bot-development"
  machine_type            = "e2-micro"
  startup_script_template = "startup-bot.sh.tpl"
  subnet_self_link        = module.network.subnet_self_link
}

module "scraper-development" {
  source                  = "../modules/compute"
  environment             = "DEVELOPMENT"
  name                    = "scraper-development"
  label                   = "filmlinkd-scraper-development"
  machine_type            = "e2-micro"
  startup_script_template = "startup-bot.sh.tpl"
  subnet_self_link        = module.network.subnet_self_link
}

# Production Computer Modules
module "bot-production" {
  source                  = "../modules/compute"
  environment             = "PRODUCTION"
  name                    = "bot-production"
  label                   = "filmlinkd-bot"
  machine_type            = "e2-medium"
  startup_script_template = "startup-bot.sh.tpl"
  subnet_self_link        = module.network.subnet_self_link
}

module "scraper-production" {
  source                  = "../modules/compute"
  environment             = "PRODUCTION"
  name                    = "scraper-production"
  label                   = "filmlinkd-scraper"
  machine_type            = "e2-medium"
  startup_script_template = "startup-scraper.sh.tpl"
  subnet_self_link        = module.network.subnet_self_link
}

module "cloudnat" {
  source            = "../modules/cloudnat"
  network_self_link = module.network.network_self_link
}

