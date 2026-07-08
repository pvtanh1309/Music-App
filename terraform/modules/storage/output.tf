output "frontend_bucket_arn" {
  value = aws_s3_bucket.frontend-music-app.arn
}

output "frontend_bucket_regional_domain_name" {
  value = aws_s3_bucket.frontend-music-app.bucket_regional_domain_name
}

output "storage_bucket_arn" {
  value = aws_s3_bucket.storage.arn
}

output "storage_bucket_regional_domain_name" {
  value = aws_s3_bucket.storage.bucket_regional_domain_name
}

output "storage_bucket_id" {
  value = aws_s3_bucket.storage.id
}

output "frontend_bucket_id" {
  value = aws_s3_bucket.frontend-music-app.id
}
