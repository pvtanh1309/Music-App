variable "vpc_id" {
    description = "ID của VPC"
    type = string
}

variable "environment" {
    description = "Tên môi trường"
    type = string
}

variable "public_subnet_az1_id" {
    description = "ID của subnet public AZ1"
    type = string
}

variable "public_subnet_az2_id" {
    description = "ID của subnet public AZ2"
    type = string
}

variable "alb_sg" {
    description = "ID của ALB Security Group"
    type = string
}

variable "ecs_sg" {
    description = "ID của ECS Security Group"
    type = string
}

variable "ecs_pull_image_role_arn" {
    description = "ARN của ECS Task Execution Role"
    type = string
}

variable "ecs_task_role_arn" {
    description = "ARN của ECS Task Role"
    type = string
}

variable "private_subnet_01_az1_id" {
    description = "ID của subnet private AZ1"
    type = string
}

variable "private_subnet_01_az2_id" {
    description = "ID của subnet private AZ2"
    type = string
}

variable "private_subnet_02_az1_id" {
    description = "ID của subnet private AZ1"
    type = string
}

variable "private_subnet_02_az2_id" {
    description = "ID của subnet private AZ2"
    type = string
}

variable "db_host" {
  type = string
}
