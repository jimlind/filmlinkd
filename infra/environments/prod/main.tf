# might be interesting to try some custom e2 instances if we can
# the bot doesn't need all the CPU that's available to it
# "e2-custom-small-4096"

module "bot-production" {
  source           = "../../modules/compute"
  environment      = "PRODUCTION"
  name             = "bot-production"
  app              = "bot"
  label            = "filmlinkd-bot-production"
  machine_type     = "e2-medium"
  max_heap_size    = "1024"
  subnet_self_link = data.terraform_remote_state.shared.outputs.subnet_self_link
}

module "scraper-production" {
  source           = "../../modules/compute"
  environment      = "PRODUCTION"
  name             = "scraper-production"
  app              = "scraper"
  label            = "filmlinkd-scraper-production"
  machine_type     = "e2-small"
  max_heap_size    = "1280"
  subnet_self_link = data.terraform_remote_state.shared.outputs.subnet_self_link
}
