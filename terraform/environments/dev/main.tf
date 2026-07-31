module "networking" {
  source      = "../../modules/networking"
  environment = var.environment
}

module "security" {
  source      = "../../modules/security"
  environment = var.environment
  vpc_id      = module.networking.vpc_id 
}

module "compute" {
  source      = "../../modules/compute"
  environment = var.environment
  vpc_id      = module.networking.vpc_id
  private_subnet_01_az1_id = module.networking.private_subnet_01_az1_id
  private_subnet_02_az1_id = module.networking.private_subnet_02_az1_id
  private_subnet_01_az2_id = module.networking.private_subnet_01_az2_id
  private_subnet_02_az2_id = module.networking.private_subnet_02_az2_id
  nat_gateway_1 = module.networking.nat_gateway_1
  nat_gateway_2 = module.networking.nat_gateway_2
  ec2_ssm_profile = module.security.ec2_ssm_profile
  alb_sg = module.security.alb_sg
  ecs_sg = module.security.ecs_sg
  ec2_db_sg = module.security.ec2_db_sg
  monitoring_sg = module.security.monitoring_sg
}

module "app" {
  source = "../../modules/app"
  vpc_id = module.networking.vpc_id
  environment = var.environment
  public_subnet_az1_id = module.networking.public_subnet_az1_id
  public_subnet_az2_id = module.networking.public_subnet_az2_id
  private_subnet_01_az1_id = module.networking.private_subnet_01_az1_id
  private_subnet_02_az1_id = module.networking.private_subnet_02_az1_id
  private_subnet_01_az2_id = module.networking.private_subnet_01_az2_id
  private_subnet_02_az2_id = module.networking.private_subnet_02_az2_id
  db_host = module.compute.db_master_private_ip
  alb_sg = module.security.alb_sg
  ecs_sg = module.security.ecs_sg
  ecs_pull_image_role_arn = module.security.ecs_pull_image_role_arn
  ecs_task_role_arn = module.security.ecs_task_role_arn
}

module "storage" {
  source = "../../modules/storage"
  environment = var.environment
}

module "cdn" {
  source     = "../../modules/cdn"
  depends_on = [module.storage]
  alb_arn    = module.app.alb_arn
  alb_dns_name = module.app.alb_dns_name
  environment = var.environment
  frontend_bucket_arn = module.storage.frontend_bucket_arn
  frontend_bucket_regional_domain_name = module.storage.frontend_bucket_regional_domain_name
  storage_bucket_arn = module.storage.storage_bucket_arn
  storage_bucket_regional_domain_name = module.storage.storage_bucket_regional_domain_name
  frontend_bucket_id = module.storage.frontend_bucket_id
  storage_bucket_id = module.storage.storage_bucket_id
}

resource "aws_s3_bucket" "terraform_state" {
  bucket = "tf-state-music-app-dev-24072026"
}

resource "aws_s3_bucket_versioning" "bucket_state_version" {
  bucket = aws_s3_bucket.terraform_state.id
  versioning_configuration {
    status = "Enabled"
  }
}

resource "aws_dynamodb_table" "terraform_state_lock" {
  name = "tf-lock-music-app-dev-24072026"
  hash_key = "LockID"
  attribute {
    name = "LockID"
    type = "S"
  }
  billing_mode = "PAY_PER_REQUEST"
}