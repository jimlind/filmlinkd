variable "environment" {
  description = "The application environment (DEVELOPMENT/PRODUCTION)."
  type        = string
}

variable "name" {
  description = "The name of the compute module."
  type        = string
}

variable "machine_type" {
  description = "The machine type to use."
  type        = string
  default     = "e2-micro"
}

variable "label" {
  description = "Used as global label for the compute module."
  type        = string
}

variable "startup_script_template" {
  description = "Path to the startup script template file"
  type        = string
}

variable "subnet_self_link" {
  description = "The self_link of the subnet to use."
  type        = string
}
