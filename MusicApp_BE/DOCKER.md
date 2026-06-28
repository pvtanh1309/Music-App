# 🐳 Docker Setup Guide

## Tổng quan

Dự án này sử dụng Docker Compose để chạy 3 services:
- **PostgreSQL**: Database chính
- **Redis**: Cache & Token management
- **Music App**: Spring Boot Backend

## 📋 Yêu cầu

- Docker Desktop (v20.10+)
- Docker Compose (v2.0+)

## 🚀 Quick Start

### 1. **Khởi động toàn bộ services**

```bash
# Using docker-compose directly
docker-compose up -d

# Or using the helper script
chmod +x docker-scripts.sh
./docker-scripts.sh start
```

### 2. **Kiểm tra status**

```bash
docker-compose ps
# Or
./docker-scripts.sh status
```

### 3. **Xem logs**

```bash
# All services
docker-compose logs -f

# Specific service
docker-compose logs -f postgres
docker-compose logs -f redis
docker-compose logs -f musicapp

# Or using script
./docker-scripts.sh logs postgres
```

## 🔧 Cấu hình

### Environment Variables (.env)

```env
# Database
DB_NAME=musicapp_db
DB_USERNAME=musicapp
DB_PASSWORD=musicapp123
DB_PORT=5432

# Redis
REDIS_PORT=6379
REDIS_PASSWORD=redis123

# App
APP_PORT=8080
JWT_SECRET=your-secret-key-here
JWT_EXPIRATION_MS=3600000
```

### Tệp cấu hình

- `compose.yaml` - Docker Compose configuration
- `Dockerfile` - Multi-stage build
- `application-prod.yml` - Production configuration
- `.env` - Environment variables
- `.dockerignore` - Files to exclude from build

## 📦 Services

### PostgreSQL

```
Host: localhost
Port: 5432
Database: musicapp_db
User: musicapp
Password: musicapp123
```

**Kết nối:**
```bash
# Using docker
docker-compose exec postgres psql -U musicapp -d musicapp_db

# Or using script
./docker-scripts.sh postgres
```

### Redis

```
Host: localhost
Port: 6379
Password: redis123
```

**Kết nối:**
```bash
# Using docker
docker-compose exec redis redis-cli -a redis123

# Or using script
./docker-scripts.sh redis
```

### Music App

```
URL: http://localhost:8080
Actuator: http://localhost:8080/actuator/health
```

## 🛠️ Helper Scripts

```bash
# Start services
./docker-scripts.sh start

# Stop services
./docker-scripts.sh stop

# Restart services
./docker-scripts.sh restart

# View logs
./docker-scripts.sh logs              # All services
./docker-scripts.sh logs postgres     # PostgreSQL
./docker-scripts.sh logs redis        # Redis
./docker-scripts.sh logs musicapp     # Music App

# Show status
./docker-scripts.sh status

# Connect to databases
./docker-scripts.sh postgres
./docker-scripts.sh redis

# Build application
./docker-scripts.sh build

# Remove everything (with volumes)
./docker-scripts.sh remove
```

## 🏗️ Build Stages

### Dockerfile Structure

**Stage 1: Builder**
- Maven image
- Build JAR từ source code
- ~650MB

**Stage 2: Runtime**
- JRE Alpine image
- Copy JAR từ builder
- Tạo non-root user
- ~300MB final image

## 🔒 Security Features

✅ **Non-root user**: App chạy với appuser (UID: 1001)  
✅ **Health checks**: Tất cả services có health check  
✅ **Data volumes**: Persistent storage cho database & cache  
✅ **Network isolation**: Custom network `musicapp-network`  
✅ **Password protected**: Redis require password  

## 📊 Data Persistence

Volumes được tạo tự động:
- `postgres_data` - PostgreSQL data
- `redis_data` - Redis data

**Xóa volumes:**
```bash
docker-compose down -v
```

## 🚨 Troubleshooting

### Container không start

```bash
# Check logs
docker-compose logs musicapp

# Rebuild
docker-compose build --no-cache musicapp
```

### Database connection error

```bash
# Restart PostgreSQL
docker-compose restart postgres

# Check health
docker-compose ps postgres
```

### Redis connection error

```bash
# Check password
redis-cli -h localhost -p 6379 -a redis123 ping

# Restart
docker-compose restart redis
```

### Port đã được sử dụng

```bash
# Change ports in .env
DB_PORT=5433
REDIS_PORT=6380
APP_PORT=8081
```

## 📝 Production Notes

⚠️ **Trước khi deploy:**

1. Thay đổi tất cả passwords trong `.env`
2. Update `JWT_SECRET` (ít nhất 32 characters)
3. Cấu hình SSL/TLS
4. Setup backup strategy cho database
5. Monitor resource usage

## 🔗 Useful Commands

```bash
# View all containers
docker ps -a

# Enter container shell
docker exec -it musicapp-backend sh

# View container stats
docker stats

# Remove dangling images
docker image prune

# Remove all unused resources
docker system prune -a
```

## 📚 Additional Resources

- [Docker Compose Documentation](https://docs.docker.com/compose/)
- [PostgreSQL Docker Image](https://hub.docker.com/_/postgres)
- [Redis Docker Image](https://hub.docker.com/_/redis)
- [Spring Boot Docker Guide](https://spring.io/guides/gs/spring-boot-docker/)
