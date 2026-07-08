output "vpc_id" {
    value = aws_vpc.main.id
}

output "public_subnet_az1_id" {
    value = aws_subnet.public_subnet_1.id
}

output "public_subnet_az2_id" {
    value = aws_subnet.public_subnet_2.id
}

output "private_subnet_01_az1_id" {
    value = aws_subnet.private_subnet_1.id
}

output "private_subnet_02_az1_id" {
    value = aws_subnet.private_subnet_2.id
}

output "private_subnet_01_az2_id" {
    value = aws_subnet.private_subnet_3.id
}

output "private_subnet_02_az2_id" {
    value = aws_subnet.private_subnet_4.id
}

output "nat_gateway_1" {
    value = aws_nat_gateway.nat_1.id
}

output "nat_gateway_2" {
    value = aws_nat_gateway.nat_2.id
}
