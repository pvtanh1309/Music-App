terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 6.0"
    }
  }
  backend "s3" {
    bucket= "tf-state-music-app-dev-24072026"
    key    = "dev/terraform.tfstate"
    region = "us-east-1"
    dynamodb_table = "tf-lock-music-app-dev-24072026"
    encrypt = true
  }
}

provider "aws" {
  region = var.aws_region
  
  default_tags {
    tags = {
      Project     = "MusicApp"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}
