output "bot_data_disk_self_link" {
  description = "The self_link of the created bot data disk"
  value       = google_compute_disk.bot_data_disk.self_link
}