module "network" {
  source = "../modules/network"
}

module "compute" {
  source           = "../modules/compute"
  subnet_self_link = module.network.subnet_self_link
}

module "cloudnat" {
  source            = "../modules/cloudnat"
  network_self_link = module.network.network_self_link
}

