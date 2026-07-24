resource "aws_lb" "alb" {
    name = "${var.environment}-alb"

    # Cho phép truy cập từ Internet
    internal = false
    load_balancer_type = "application"
    security_groups = [var.alb_sg]
    subnets = [var.public_subnet_az1_id, var.public_subnet_az2_id]

    tags = {
        Name = "ALB-musicApp"
        Project = "Music-App"
    }
}

resource "aws_lb_target_group" "aws_lb_target_group" {
    name = "${var.environment}-alb-tg-8080"

    port = 8080       
    protocol = "HTTP"
    vpc_id = var.vpc_id
    target_type = "ip"

    health_check {
        enabled = true

        # Mỗi 30s kiểm tra 1 lần
        interval = 30

        # Timeout 10s nếu không response
        timeout = 10

        # 3 lần liên tiếp thất bại thì đánh giá là unhealthy
        unhealthy_threshold = 3

        # 3 lần liên tiếp thành công thì đánh giá là healthy
        healthy_threshold = 3

        # Chỉ chấp nhận response HTTP 200 OK
        matcher = "200"

        # URL Kiểm tra
        path = "/actuator/health"    # Sau này sửa thành /actuator/health
    }

   lifecycle {
       create_before_destroy = true
   }
}

# Load Balancer Listener
resource "aws_lb_listener" "alb_listener" {
    load_balancer_arn = aws_lb.alb.arn
    port = 80
    protocol = "HTTP"

    # Nếu không có rule nào khác thì chuyển tiếp đến target group
    default_action {
        type = "forward"
        target_group_arn = aws_lb_target_group.aws_lb_target_group.arn
    }
}

# ECR Repository
resource "aws_ecr_repository" "music_app" {
  name = "${var.environment}-music-app"
  force_delete = true

  tags = {
    Name = "${var.environment}-music-app"
    Project = "Music-App"
  }
}

# ECS Cluster
resource "aws_ecs_cluster" "app-Cluster" {
    name = "${var.environment}-app-cluster"

    # Không thu thập logs cho cluster này
    setting {
        name = "containerInsights"
        value = "disabled"
    }

    tags = {
        Name = "${var.environment}-app-cluster"
        Project = "Music-App"
    }
}

resource "aws_ecs_task_definition" "ecs-task-def" {
    family = "${var.environment}-music-app-task-def"

    # Farget chỉ hỗ trợ awsvpc
    network_mode = "awsvpc"

    # Task được chạy trên hạ tầng Fargate
    requires_compatibilities = ["FARGATE"]

    # Cấu hình tài nguyên cho task (CPU: 0.5 Core, Memory: 1GB)
    cpu = 512
    memory = 1024 

    execution_role_arn = var.ecs_pull_image_role_arn
    task_role_arn = var.ecs_task_role_arn

    # Định nghĩa container
    container_definitions = jsonencode([
        {
            name = "music_app_container"
            image = "nginxdemos/hello:plain-text"
            portMappings = [
                {
                    containerPort = 8080   
                    protocol = "tcp"
                }
            ]
            environment = [
                { name = "SPRING_DATASOURCE_URL", value = "jdbc:postgresql://10.0.4.14:5433/SoundCloudDB"},
                { name = "SPRING_DATASOURCE_USERNAME", value = "app_user" },
                { name = "SPRING_DATASOURCE_PASSWORD", value = "app_user_1309" },
                { name = "SPRING_REDIS_HOST", value = "10.0.4.14" },
                { name = "SPRING_REDIS_PORT", value = "6379" }
            ]
        }
    ])
}

resource "aws_ecs_service" "music-app-service" {
    name = "${var.environment}-music-app-service"
    cluster = aws_ecs_cluster.app-Cluster.id
    task_definition = aws_ecs_task_definition.ecs-task-def.arn
    desired_count = 1
    launch_type = "FARGATE"

    depends_on = [
        aws_lb_listener.alb_listener,
        aws_lb_target_group.aws_lb_target_group,
        aws_lb.alb,
        aws_ecr_repository.music_app,
        aws_ecs_cluster.app-Cluster,
        aws_ecs_task_definition.ecs-task-def
    ]

    network_configuration {
        subnets = [var.private_subnet_01_az1_id, var.private_subnet_01_az2_id]
        security_groups = [var.ecs_sg]
        assign_public_ip = false
    }

    load_balancer {
        target_group_arn = aws_lb_target_group.aws_lb_target_group.arn
        container_name = "music_app_container"
        container_port = 8080
    }

    lifecycle {
        ignore_changes = [task_definition]
    }
}
