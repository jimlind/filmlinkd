module "bot-development" {
  source           = "../../modules/compute"
  environment      = "DEVELOPMENT"
  name             = "bot-development"
  app              = "bot"
  label            = "filmlinkd-bot-development"
  machine_type     = "e2-micro"
  max_heap_size    = "256"
  subnet_self_link = data.terraform_remote_state.shared.outputs.subnet_self_link
}

module "scraper-development" {
  source           = "../../modules/compute"
  environment      = "DEVELOPMENT"
  name             = "scraper-development"
  app              = "scraper"
  label            = "filmlinkd-scraper-development"
  machine_type     = "e2-micro"
  max_heap_size    = "256"
  subnet_self_link = data.terraform_remote_state.shared.outputs.subnet_self_link
}