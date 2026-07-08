# 1. Tạo Trust Policy (Ai được phép SD role)
data "aws_iam_policy_document" "ec2_assume_role" {
  statement {
    actions = ["sts:AssumeRole"]
    
    principals {
      type        = "Service"
      identifiers = ["ec2.amazonaws.com"]
    }
  }
}

# Tạo IAM Role cho EC2
resource "aws_iam_role" "ec2_ssm_role" {
  name               = "${var.environment}-ec2-ssm-role"
  assume_role_policy = data.aws_iam_policy_document.ec2_assume_role.json
}

# Gắn chính sách (Policy) của AWS cấp sẵn: "AmazonSSMManagedInstanceCore" vào Role (Role này được phép làm gì?)
resource "aws_iam_role_policy_attachment" "ssm_core" {
  role       = aws_iam_role.ec2_ssm_role.name
  policy_arn = "arn:aws:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

# Tạo Instance Profile để sau này gắn vào cái máy EC2
resource "aws_iam_instance_profile" "ec2_ssm_profile" {
  name = "${var.environment}-ec2-ssm-profile"
  role = aws_iam_role.ec2_ssm_role.name
}

#--- IAM Role dành cho ECS Fargate Task (PULL IMAGE ECR) ---
data "aws_iam_policy_document" "ecs_execution" {
  statement {
    actions = ["sts:AssumeRole"]
    
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_pull_image_role" {
  name = "${var.environment}-ecs-pull-image-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_execution.json

  tags = {
    Name = "${var.environment}-ecs-pull-image-role"
    Project = "Music-App"
  }
}

resource "aws_iam_role_policy_attachment" "ecs_pull_image_role_attach" {
    role = aws_iam_role.ecs_pull_image_role.name
    policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

#--- IAM Role ECS Task (Cho phép đọc file từ S3 và lấy mật khẩu từ SSM) ---
data "aws_iam_policy_document" "ecs_task" {
  statement {
    actions = ["sts:AssumeRole"]
    
    principals {
      type        = "Service"
      identifiers = ["ecs-tasks.amazonaws.com"]
    }
  }
}

resource "aws_iam_role" "ecs_task_role" {
  name = "${var.environment}-ecs-task-role"
  assume_role_policy = data.aws_iam_policy_document.ecs_task.json

  tags = {
    Name = "${var.environment}-ecs-task-role"
    Project = "Music-App"
  }
}




