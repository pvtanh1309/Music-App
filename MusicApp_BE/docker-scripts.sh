#!/bin/bash

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored output
print_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Check if docker and docker-compose are installed
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker is not installed"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose is not installed"
        exit 1
    fi
    
    print_info "Docker and Docker Compose are installed"
}

# Function to start all services
start_services() {
    print_info "Starting all services..."
    docker-compose up -d
    
    if [ $? -eq 0 ]; then
        print_info "All services started successfully"
        print_info "Services:"
        print_info "  - PostgreSQL: localhost:5432"
        print_info "  - Redis: localhost:6379"
        print_info "  - Music App: http://localhost:8080"
    else
        print_error "Failed to start services"
        exit 1
    fi
}

# Function to stop all services
stop_services() {
    print_info "Stopping all services..."
    docker-compose down
    
    if [ $? -eq 0 ]; then
        print_info "All services stopped successfully"
    else
        print_error "Failed to stop services"
        exit 1
    fi
}

# Function to remove all services and volumes
remove_services() {
    print_warning "This will remove all services and their data!"
    read -p "Are you sure? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        print_info "Removing all services and volumes..."
        docker-compose down -v
        
        if [ $? -eq 0 ]; then
            print_info "All services and volumes removed successfully"
        else
            print_error "Failed to remove services"
            exit 1
        fi
    fi
}

# Function to view logs
view_logs() {
    local service=$1
    if [ -z "$service" ]; then
        print_info "Viewing logs for all services..."
        docker-compose logs -f
    else
        print_info "Viewing logs for $service..."
        docker-compose logs -f $service
    fi
}

# Function to show service status
show_status() {
    print_info "Service Status:"
    docker-compose ps
}

# Function to connect to PostgreSQL
connect_postgres() {
    print_info "Connecting to PostgreSQL..."
    docker-compose exec postgres psql -U musicapp -d musicapp_db
}

# Function to connect to Redis
connect_redis() {
    print_info "Connecting to Redis..."
    docker-compose exec redis redis-cli -a redis123
}

# Function to build the application
build_app() {
    print_info "Building application..."
    docker-compose build --no-cache musicapp
    
    if [ $? -eq 0 ]; then
        print_info "Application built successfully"
    else
        print_error "Failed to build application"
        exit 1
    fi
}

# Main script
case "$1" in
    start)
        check_docker
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        stop_services
        sleep 2
        start_services
        ;;
    remove)
        remove_services
        ;;
    logs)
        view_logs $2
        ;;
    status)
        show_status
        ;;
    postgres)
        connect_postgres
        ;;
    redis)
        connect_redis
        ;;
    build)
        build_app
        ;;
    *)
        echo "Usage: $0 {start|stop|restart|remove|logs [service]|status|postgres|redis|build}"
        echo ""
        echo "Examples:"
        echo "  $0 start              - Start all services"
        echo "  $0 stop               - Stop all services"
        echo "  $0 restart            - Restart all services"
        echo "  $0 remove             - Remove all services and volumes"
        echo "  $0 logs               - View logs from all services"
        echo "  $0 logs postgres      - View logs from PostgreSQL"
        echo "  $0 logs redis         - View logs from Redis"
        echo "  $0 logs musicapp      - View logs from Music App"
        echo "  $0 status             - Show service status"
        echo "  $0 postgres           - Connect to PostgreSQL"
        echo "  $0 redis              - Connect to Redis"
        echo "  $0 build              - Build the application"
        exit 1
        ;;
esac
