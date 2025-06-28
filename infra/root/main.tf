module "disk_module" {
  source = "../stacks/disks"
}

module "compute_module" {
  source                           = "../stacks/compute"
  attached_bot_data_disk_self_link = module.disk_module.bot_data_disk_self_link
}