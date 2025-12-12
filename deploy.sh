#!/bin/bash

# Innovation Management System - Deploy Script for Linux
# Author: NguyenThanhNhut13
# Description: Automated deployment script

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Functions
print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_info() {
    echo -e "${YELLOW}ℹ $1${NC}"
}

# Check if running as root
if [ "$EUID" -eq 0 ]; then 
    print_error "Please do not run as root"
    exit 1
fi

print_info "Starting deployment process..."

# Step 1: Check prerequisites
print_info "Checking prerequisites..."

if ! command -v docker &> /dev/null; then
    print_error "Docker is not installed. Please install Docker first."
    exit 1
fi
print_success "Docker is installed"

if ! command -v docker compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi
print_success "Docker Compose is installed"

if ! command -v git &> /dev/null; then
    print_error "Git is not installed. Please install Git first."
    exit 1
fi
print_success "Git is installed"

# Step 2: Check .env file
if [ ! -f .env ]; then
    print_error ".env file not found!"
    print_info "Creating .env from .env.example..."
    if [ -f .env.example ]; then
        cp .env.example .env
        print_info "Please edit .env file with your actual credentials"
        print_info "Run: nano .env"
        exit 1
    else
        print_error ".env.example not found!"
        exit 1
    fi
fi
print_success ".env file exists"

# Step 3: Check RSA keys
if [ ! -f src/main/resources/keys/public_key.pem ] || [ ! -f src/main/resources/keys/private_key.pem ]; then
    print_error "RSA keys not found in src/main/resources/keys/"
    print_info "Please copy your keys to src/main/resources/keys/"
    exit 1
fi
print_success "RSA keys found"

# Step 4: Clean up old network if exists
print_info "Checking Docker network..."
if docker network inspect innovation-network >/dev/null 2>&1; then
    print_info "Removing old network..."
    docker compose down || true
    docker stop $(docker ps -q) 2>/dev/null || true
    docker network rm innovation-network 2>/dev/null || true
fi
print_success "Network will be created by docker-compose"

# Step 5: Stop old containers
print_info "Stopping old containers..."
docker compose down || true
print_success "Old containers stopped"

# Step 6: Pull latest code (optional)
read -p "Do you want to pull latest code from Git? (y/n) " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    print_info "Pulling latest code..."
    git pull
    print_success "Code updated"
fi

# Step 7: Build and start services
print_info "Building and starting services..."
docker compose up -d --build

# Step 8: Wait for services to be ready
print_info "Waiting for services to start..."
sleep 30

# Step 9: Health checks
print_info "Running health checks..."

# Check PostgreSQL
if docker exec postgres pg_isready -U postgresADMIN >/dev/null 2>&1; then
    print_success "PostgreSQL is healthy"
else
    print_error "PostgreSQL is not responding"
fi

# Check Redis
if docker exec redis redis-cli ping >/dev/null 2>&1; then
    print_success "Redis is healthy"
else
    print_error "Redis is not responding"
fi

# Check MinIO
if curl -f http://localhost:9000/minio/health/live >/dev/null 2>&1; then
    print_success "MinIO is healthy"
else
    print_error "MinIO is not responding"
fi

# Check Application
if curl -f http://localhost:8081/actuator/health >/dev/null 2>&1; then
    print_success "Application is healthy"
else
    print_error "Application is not responding yet (may need more time)"
fi

# Check Jenkins
if curl -f http://localhost:8080 >/dev/null 2>&1; then
    print_success "Jenkins is running"
else
    print_error "Jenkins is not responding yet (may need more time)"
fi

# Check Python AI Embedding Service (internal service - check via docker exec)
if docker exec innovation-ai-embedding-service curl -f http://localhost:8000/health >/dev/null 2>&1; then
    print_success "Python AI Embedding Service is healthy"
else
    print_error "Python AI Embedding Service is not responding yet (may need more time - model download on first start)"
fi

# Step 10: Display status
print_info "Deployment completed!"
echo ""
echo "Services status:"
docker compose ps
echo ""
echo "Access URLs:"
echo "  - Application: http://localhost:8081"
echo "  - Jenkins: http://localhost:8080"
echo "  - MinIO Console: http://localhost:9001"
echo ""
echo "Useful commands:"
echo "  - View logs: docker compose logs -f"
echo "  - Stop services: docker compose down"
echo "  - Restart service: docker compose restart <service_name>"
echo ""

# Get Jenkins initial password if first time
if [ -f "$(docker volume inspect jenkins_home --format '{{ .Mountpoint }}')/secrets/initialAdminPassword" 2>/dev/null ]; then
    print_info "Jenkins initial admin password:"
    docker exec jenkins cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || echo "Not available yet"
fi
