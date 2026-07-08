data "aws_ami" "amazon_linux_2023" {
    most_recent = true 
    owners      = ["amazon"]

    filter {
        name = "name"
        values = ["al2023-ami-2023.*-x86_64"]
    }

    filter {
        name ="virtualization-type"
        values = ["hvm"]
    }
}

resource "aws_instance" "db_master" {
    ami = data.aws_ami.amazon_linux_2023.id

    instance_type = "t3.small"

    subnet_id = var.private_subnet_02_az1_id

    vpc_security_group_ids = [var.ec2_db_sg]

    iam_instance_profile = var.ec2_ssm_profile

    user_data = file("${path.module}/scripts/docker.sh")

    tags = {
        Name = "${var.environment}-db-master"
        Project = "music-app"
    }
}

resource "aws_instance" "db_slave" {
    ami = data.aws_ami.amazon_linux_2023.id

    instance_type = "t3.small"

    subnet_id = var.private_subnet_02_az2_id

    vpc_security_group_ids = [var.ec2_db_sg]

    iam_instance_profile = var.ec2_ssm_profile

    user_data = file("${path.module}/scripts/docker.sh")

    tags = {
        Name = "${var.environment}-db-slave"
        Project = "music-app"
    }
}

resource "aws_instance" "monitoring_server" {
    ami = data.aws_ami.amazon_linux_2023.id

    instance_type = "t3.micro"

    subnet_id = var.private_subnet_01_az1_id

    vpc_security_group_ids = [var.monitoring_sg]

    iam_instance_profile = var.ec2_ssm_profile

    user_data = file("${path.module}/scripts/docker.sh")

    tags = {
        Name = "${var.environment}-monitoring-server"
        Project = "music-app"
    }
}
