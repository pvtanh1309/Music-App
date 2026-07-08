output "ec2_ssm_profile" {
    value = aws_iam_instance_profile.ec2_ssm_profile.name
}

output "alb_sg" {
    value = aws_security_group.alb_sg.id
}

output "ecs_sg" {
    value = aws_security_group.ecs_sg.id
}

output "ec2_db_sg" {
    value = aws_security_group.ec2_db_sg.id
}

output "monitoring_sg" {
    value = aws_security_group.monitoring_sg.id
}

output "ecs_pull_image_role_arn" {
    value = aws_iam_role.ecs_pull_image_role.arn
}

output "ecs_task_role_arn" {
    value = aws_iam_role.ecs_task_role.arn
}