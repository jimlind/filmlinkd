module "firestore" {
  source = "../modules/firestore"
}

module "pubsub" {
  source = "../modules/pubsub"
}

module "secret-manager" {
  source = "../modules/secret-manager"
}