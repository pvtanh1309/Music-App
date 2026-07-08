# Khởi tạo VPC 
resource "aws_vpc" "main" {
    cidr_block = "10.0.0.0/16"

    
    enable_dns_hostnames = true
    enable_dns_support   = true
    
    tags = {
        Name    = "${var.environment}-vpc"
        Project = "music-app"
    }
}

# Internet gateway
resource "aws_internet_gateway" "igw" {
    vpc_id = aws_vpc.main.id 

    tags = {
        Name    = "${var.environment}-igw"
        Project = "music-app"
    }
}

# Public subnet 1
resource "aws_subnet" "public_subnet_1" {
    vpc_id                  = aws_vpc.main.id
    cidr_block              = "10.0.1.0/24"
    availability_zone       = "us-east-1a"

    # Tự động cấp IP Public cho máy chủ 
    map_public_ip_on_launch = true      

    tags = {
        Name    = "${var.environment}-public-subnet-1"
        Project = "music-app"
    }
}

# Public subnet 2
resource "aws_subnet" "public_subnet_2" {
    vpc_id                  = aws_vpc.main.id
    cidr_block              = "10.0.3.0/24"
    availability_zone       = "us-east-1b"

    # Tự động cấp IP Public cho máy chủ 
    map_public_ip_on_launch = true      

    tags = {
        Name    = "${var.environment}-public-subnet-2"
        Project = "music-app"
    }
}

# Private subnet 1
resource "aws_subnet" "private_subnet_1" {
    vpc_id                  = aws_vpc.main.id
    cidr_block              = "10.0.2.0/24"
    availability_zone       = "us-east-1a" 

    tags = {
        Name    = "${var.environment}-private-subnet-1"
        Project = "music-app"
    }
}

# Private subnet 2
resource "aws_subnet" "private_subnet_2" {
    vpc_id                  = aws_vpc.main.id
    cidr_block              = "10.0.4.0/24"
    availability_zone       = "us-east-1a" 

    tags = {
        Name    = "${var.environment}-private-subnet-2"
        Project = "music-app"
    }
}

resource "aws_subnet" "private_subnet_3" {
    vpc_id = aws_vpc.main.id
    cidr_block = "10.0.5.0/24"
    availability_zone = "us-east-1b"
    
    tags = {
        Name = "${var.environment}-private-subnet-3"
        Project = "music-app"
    }
}

resource "aws_subnet" "private_subnet_4" {
    vpc_id = aws_vpc.main.id
    cidr_block = "10.0.6.0/24"
    availability_zone = "us-east-1b"
    

    tags = {
        Name = "${var.environment}-private-subnet-4"
        Project = "music-app"
    }
}

# Elastic IP cho NAT ở Public Subnet 1
resource "aws_eip" "nat_eip_1" {
    domain = "vpc"
    depends_on = [aws_internet_gateway.igw]
}

# Elastic IP cho NAT ở Public Subnet 2
resource "aws_eip" "nat_eip_2" {
    domain = "vpc"
    depends_on = [aws_internet_gateway.igw]
}

# NAT Gateway Subnet 1
resource "aws_nat_gateway" "nat_1" {
    allocation_id = aws_eip.nat_eip_1.id
    subnet_id = aws_subnet.public_subnet_1.id 

    depends_on = [aws_internet_gateway.igw]
    tags = {
        Name = "${var.environment}-nat-1"
        Project = "music-app"
    }
}

# NAT Gateway Subnet 2
resource "aws_nat_gateway" "nat_2" {
    allocation_id = aws_eip.nat_eip_2.id
    subnet_id = aws_subnet.public_subnet_2.id 

    depends_on = [aws_internet_gateway.igw]

    tags = {
        Name = "${var.environment}-nat-2"
        Project = "music-app"
    }
}

# Route Table Public Subnet 
resource "aws_route_table" "public_table" {
    vpc_id = aws_vpc.main.id

    route {
        cidr_block = "0.0.0.0/0"
        gateway_id = aws_internet_gateway.igw.id
    }

    tags = {
        Name = "${var.environment}-public-route-1"
        Project = "music-app"
    }
}

# Gắn public table vào public subnet 
resource "aws_route_table_association" "public_1" {
    subnet_id = aws_subnet.public_subnet_1.id
    route_table_id = aws_route_table.public_table.id
}

resource "aws_route_table_association" "public_2" {
    subnet_id = aws_subnet.public_subnet_2.id
    route_table_id = aws_route_table.public_table.id
}

# Tạo route table cho private subnet AZ1
resource "aws_route_table" "private_subnet_az_1" {
    vpc_id = aws_vpc.main.id

    # Chỉ cho phép traffic nội bộ trong VPC
    route {
        cidr_block = "0.0.0.0/0"
        nat_gateway_id = aws_nat_gateway.nat_1.id
    }
}

# Gắn private table vào private subnet AZ1
resource "aws_route_table_association" "private_1" {
    subnet_id = aws_subnet.private_subnet_1.id
    route_table_id = aws_route_table.private_subnet_az_1.id
}

resource "aws_route_table_association" "private_2" {
    subnet_id = aws_subnet.private_subnet_2.id
    route_table_id = aws_route_table.private_subnet_az_1.id
}

# Tạo route table cho private subnet AZ2
resource "aws_route_table" "private_subnet_az_2" {
    vpc_id = aws_vpc.main.id

    # Chỉ cho phép traffic nội bộ trong VPC
    route {
        cidr_block = "0.0.0.0/0"
        nat_gateway_id = aws_nat_gateway.nat_2.id
    }
}

# Gắn private table vào private subnet AZ2
resource "aws_route_table_association" "private_3" {
    subnet_id = aws_subnet.private_subnet_3.id
    route_table_id = aws_route_table.private_subnet_az_2.id
}

resource "aws_route_table_association" "private_4" {
    subnet_id = aws_subnet.private_subnet_4.id
    route_table_id = aws_route_table.private_subnet_az_2.id
}
