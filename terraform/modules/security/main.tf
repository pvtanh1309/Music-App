# 1. Security Group cho ALB (Load Balancer) - Nằm ở Public Subnet, chỉ mở 2 cổng http, https
resource "aws_security_group" "alb_sg" {
  name        = "${var.environment}-alb-sg"
  description = "Security Group for Application Load Balancer"
  vpc_id      = var.vpc_id

  tags = {
    Name = "${var.environment}-alb-sg"
    Project = "Music-App"
  }
}

resource "aws_vpc_security_group_ingress_rule" "allow_http" {
  security_group_id = aws_security_group.alb_sg.id
  description = "Allow HTTP Traffic from Internet to ALB"
  
  cidr_ipv4 = "0.0.0.0/0"
  from_port = 80
  to_port = 80
  ip_protocol = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "allow_https" {
  security_group_id = aws_security_group.alb_sg.id
  description = "Allow HTTPS Traffic from Internet to ALB"
  
  cidr_ipv4 = "0.0.0.0/0"
  from_port = 443
  to_port = 443
  ip_protocol = "tcp"
}

resource "aws_vpc_security_group_egress_rule" "alb_allow_all" {
  security_group_id = aws_security_group.alb_sg.id
  description = "Allow All Traffic from ALB to Internet"
  
  cidr_ipv4 = "0.0.0.0/0"
  ip_protocol = "-1"
}

# 2. SG cho ECS Fargate - Mở port 8080, đi vào từ ALB 
resource "aws_security_group" "ecs_sg" {
  name        = "${var.environment}-ecs-sg"
  description = "Security Group for ECS Fargate"
  vpc_id      = var.vpc_id

  tags = {
    Name = "${var.environment}-ecs-sg"
    Project = "Music-App"
  }
}

resource "aws_vpc_security_group_ingress_rule" "allow_alb" {
  security_group_id = aws_security_group.ecs_sg.id
  referenced_security_group_id = aws_security_group.alb_sg.id

  from_port = 80
  to_port = 80
  ip_protocol = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "allow_prometheus_metric" {
  security_group_id = aws_security_group.ecs_sg.id
  referenced_security_group_id = aws_security_group.monitoring_sg.id

  from_port = 8080
  to_port = 8080
  ip_protocol = "tcp"
}

resource "aws_vpc_security_group_egress_rule" "ecs_allow_all" {
  security_group_id = aws_security_group.ecs_sg.id
  
  cidr_ipv4 = "0.0.0.0/0"
  ip_protocol = "-1"
}

# 3. Security Group cho EC2 chứa Redis và PostgreSQL
resource "aws_security_group" "ec2_db_sg" {
  name        = "${var.environment}-ec2-db-sg"
  description = "Security Group for EC2 containing Redis and PostgreSQL"
  vpc_id      = var.vpc_id

  tags = {
    Name = "${var.environment}-ec2-db-sg"
    Project = "Music-App"
  }
}

resource "aws_vpc_security_group_ingress_rule" "allow_redis" {
  security_group_id = aws_security_group.ec2_db_sg.id
  referenced_security_group_id = aws_security_group.ecs_sg.id

  from_port = 6379
  to_port = 6379
  ip_protocol = "tcp"
}

resource "aws_vpc_security_group_ingress_rule" "allow_postgresql" {
  security_group_id = aws_security_group.ec2_db_sg.id
  referenced_security_group_id = aws_security_group.ecs_sg.id

  from_port = 5432
  to_port = 5432
  ip_protocol = "tcp"
}

# Node Exporter (Port 9100): Đo đạc CPU, RAM, Ổ cứng của bản thân con EC2.
resource "aws_vpc_security_group_ingress_rule" "allow_node_exporter" {
  security_group_id = aws_security_group.ec2_db_sg.id
  referenced_security_group_id = aws_security_group.monitoring_sg.id

  from_port = 9100
  to_port = 9100
  ip_protocol = "tcp"
}

# Postgres Exporter (Port 9187): Đo đạc tốc độ truy vấn SQL
resource "aws_vpc_security_group_ingress_rule" "allow_postgres_exporter" {
  security_group_id = aws_security_group.ec2_db_sg.id
  referenced_security_group_id = aws_security_group.monitoring_sg.id

  from_port = 9187
  to_port = 9187
  ip_protocol = "tcp"
}

# Redis Exporter (Port 9121): Đo đạc tốc độ truy vấn Redis, số lượng cache hit/miss.
resource "aws_vpc_security_group_ingress_rule" "allow_redis_exporter" {
  security_group_id = aws_security_group.ec2_db_sg.id
  referenced_security_group_id = aws_security_group.monitoring_sg.id

  from_port = 9121
  to_port = 9121
  ip_protocol = "tcp"
}

resource "aws_vpc_security_group_egress_rule" "db_allow_all" {
  security_group_id = aws_security_group.ec2_db_sg.id
  
  cidr_ipv4 = "0.0.0.0/0"
  ip_protocol = "-1"
}

# 4. Security Group cho monitoring
resource "aws_security_group" "monitoring_sg" {
  name        = "${var.environment}-monitoring-sg"
  description = "Security Group for monitoring"
  vpc_id      = var.vpc_id

  tags = {
    Name = "${var.environment}-monitoring-sg"
    Project = "Music-App"
  }
}

resource "aws_vpc_security_group_egress_rule" "monitoring_allow_all" {
  security_group_id = aws_security_group.monitoring_sg.id
  
  cidr_ipv4 = "0.0.0.0/0"
  ip_protocol = "-1"
}







