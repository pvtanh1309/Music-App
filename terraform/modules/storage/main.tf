resource "aws_s3_bucket" "storage" {
    bucket = "${var.environment}-storage-song-avatar-1309"

    force_destroy = true

    tags = {
        Name = "${var.environment}-storage-song-avatar"
        Project = "music-app"
    }
}

resource "aws_s3_bucket" "frontend-music-app" {
    bucket = "${var.environment}-frontend-music-app-1309"

    force_destroy = true

    tags = {
        Name = "${var.environment}-frontend-music-app"
        Project = "music-app"
    }
}

resource "aws_s3_bucket_public_access_block" "frontend_bucket_access_block" {
  bucket = aws_s3_bucket.frontend-music-app.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_public_access_block" "storage_bucket_access_block" {
  bucket = aws_s3_bucket.storage.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}



