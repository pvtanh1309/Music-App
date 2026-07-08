variable "environment" {
    description = "Tên môi trường"
    type = string
}

variable "vpc_id" {
    description = "ID của VPC"
    type = string
}

variable "private_subnet_01_az1_id" {
    description = "ID của subnet private 01 AZ1"
    type = string
}

variable "private_subnet_02_az1_id" {
    description = "ID của subnet private 02 AZ1"
    type = string
}

variable "private_subnet_01_az2_id" {
    description = "ID của subnet private 01 AZ2"
    type = string
}

variable "private_subnet_02_az2_id" {
    description = "ID của subnet private 02 AZ2"
    type = string
}

variable "nat_gateway_1" {
    description = "ID của NAT Gateway 1"
    type = string
}

variable "nat_gateway_2" {
    description = "ID của NAT Gateway 2"
    type = string
}

variable "ec2_ssm_profile" {
    description = "ID của EC2 SSM Profile"
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

variable "ec2_db_sg" {
    description = "ID của EC2 DB Security Group"
    type = string
}

variable "monitoring_sg" {
    description = "ID của Monitoring Security Group"
    type = string
}