data "aws_iam_policy_document" "frontend_bucket_policy" {
  statement {
    sid    = "AllowCloudFrontServicePrincipalReadWrite"
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }

    actions = [
      "s3:GetObject"
    ]

    resources = [
      "${var.frontend_bucket_arn}/*"
    ]

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values   = [aws_cloudfront_distribution.s3_distribution.arn]
    }
  }
}

data "aws_iam_policy_document" "storage_bucket_policy" {
  statement {
    sid    = "AllowCloudFrontServicePrincipalReadWrite"
    effect = "Allow"

    principals {
      type        = "Service"
      identifiers = ["cloudfront.amazonaws.com"]
    }

    actions = [
      "s3:GetObject"
    ]

    resources = [
      "${var.storage_bucket_arn}/*"
    ]

    condition {
      test     = "StringEquals"
      variable = "AWS:SourceArn"
      values   = [aws_cloudfront_distribution.s3_distribution.arn]
    }
  }
}

resource "aws_s3_bucket_policy" "frontend_bucket_policy" {
  bucket = var.frontend_bucket_id
  policy = data.aws_iam_policy_document.frontend_bucket_policy.json
}

resource "aws_s3_bucket_policy" "storage_bucket_policy" {
  bucket = var.storage_bucket_id
  policy = data.aws_iam_policy_document.storage_bucket_policy.json
}

resource "aws_cloudfront_origin_access_control" "default" {
  name                              = "default-oac"
  origin_access_control_origin_type = "s3"
  signing_behavior                  = "always"
  signing_protocol                  = "sigv4"
}

resource "aws_cloudfront_distribution" "s3_distribution" {
    enabled = true

    origin {
        domain_name              = var.frontend_bucket_regional_domain_name
        origin_access_control_id = aws_cloudfront_origin_access_control.default.id
        origin_id = "frontend-s3"
    }
    origin {
        domain_name              = var.storage_bucket_regional_domain_name
        origin_access_control_id = aws_cloudfront_origin_access_control.default.id
        origin_id = "storage-s3"
    }
    origin {
        domain_name = var.alb_dns_name
        origin_id = "backend-alb"

        custom_origin_config {
            origin_protocol_policy = "http-only"
            http_port = 80
            https_port = 443
            origin_ssl_protocols = ["TLSv1.2"]

        }
    }

    # 1. Đường chính: Vào trang chủ (Trỏ về S3 Frontend)
    default_cache_behavior {
        target_origin_id = "frontend-s3"
        viewer_protocol_policy = "redirect-to-https"
        allowed_methods = ["GET", "HEAD"]
        cached_methods = ["GET", "HEAD"]
        cache_policy_id = "658327ea-f89d-4fab-a63d-7e88639e58f6" # AWS Managed: CachingOptimized
    }

    # 2. Đường rẽ nhánh 1: Gọi Backend API (Trỏ về ALB)
    ordered_cache_behavior {
        path_pattern = "/api/*"
        target_origin_id = "backend-alb"
        viewer_protocol_policy = "redirect-to-https"
        allowed_methods = ["GET", "HEAD", "OPTIONS", "PUT", "POST", "PATCH", "DELETE"]
        cached_methods = ["GET", "HEAD", "OPTIONS"]
        cache_policy_id = "4135ea2d-6df8-44a3-9df3-4b5a84be39ad" # AWS Managed: CachingDisabled (Tuyệt đối không cache API)
    }

    # 3. Đường rẽ nhánh 2: Nghe nhạc, xem ảnh (Trỏ về S3 Storage)
    ordered_cache_behavior {
        path_pattern = "/media/*"
        target_origin_id = "storage-s3"
        viewer_protocol_policy = "redirect-to-https"
        allowed_methods = ["GET", "HEAD"]
        cached_methods = ["GET", "HEAD"]
        cache_policy_id = "658327ea-f89d-4fab-a63d-7e88639e58f6" # AWS Managed: CachingOptimized
    }

    # Cấu hình SSL/TLS (Bắt buộc)
    viewer_certificate {
        cloudfront_default_certificate = true
    }

    # Không giới hạn quốc gia truy cập (Bắt buộc)
    restrictions {
        geo_restriction {
            restriction_type = "none"
        }
    }

    tags = {
        Name = "S3-Distribution"
        Project = "Music-App"
    }
}