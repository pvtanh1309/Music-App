#!/bin/bash

# Update OS and install Docker 
sudo dnf update -y
sudo dnf upgrade -y

# Install Docker
sudo dnf install docker -y
sudo systemctl start docker 
sudo systemctl enable docker 

# Add ec2-user to docker group
sudo usermod -aG docker ec2-user

# Install Docker compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose

# Cấp quyền thực thi cho file
sudo chmod +x /usr/local/bin/docker-compose