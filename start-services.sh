#!/bin/bash

# Innovation Management System - Service Startup Script
# This script starts all services using docker-compose

echo "🚀 Starting Innovation Management System Services..."
echo "=================================================="

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker is not running. Please start Docker first."
    exit 1
fi

# Check if docker-compose is available
if ! command -v docker-compose &> /dev/null; then
    echo "❌ docker-compose is not installed. Please install docker-compose first."
    exit 1
fi

# Create necessary directories
echo "📁 Creating necessary directories..."
mkdir -p init-scripts
mkdir -p jenkins-config

# Set proper permissions for Jenkins
echo "🔐 Setting permissions for Jenkins..."
sudo chown -R 1000:1000 jenkins-config/ 2>/dev/null || true

# Start all services
echo "🐳 Starting all services with docker-compose..."
docker-compose up -d

# Wait for services to be ready
echo "⏳ Waiting for services to be ready..."

# Wait for PostgreSQL
echo "📊 Waiting for PostgreSQL..."
timeout 60 bash -c 'until docker exec postgres pg_isready -U postgres -d innovation_management; do sleep 2; done'
echo "✅ PostgreSQL is ready!"

# Wait for Redis
echo "🔴 Waiting for Redis..."
timeout 30 bash -c 'until docker exec redis redis-cli ping; do sleep 2; done'
echo "✅ Redis is ready!"

# Wait for MinIO
echo "📦 Waiting for MinIO..."
timeout 30 bash -c 'until docker exec minio curl -f http://localhost:9000/minio/health/live; do sleep 2; done'
echo "✅ MinIO is ready!"

# Wait for Jenkins
echo "🔧 Waiting for Jenkins..."
timeout 60 bash -c 'until docker exec jenkins curl -f http://localhost:8080/login; do sleep 5; done'
echo "✅ Jenkins is ready!"

# Wait for Backend
echo "⚙️ Waiting for Backend..."
timeout 120 bash -c 'until docker exec backend curl -f http://localhost:8080/api/v1/utils/ping; do sleep 5; done'
echo "✅ Backend is ready!"

echo ""
echo "🎉 All services are running successfully!"
echo "=================================================="
echo "📋 Service URLs:"
echo "   Backend API:     http://localhost:8080"
echo "   Jenkins:         http://localhost:8081"
echo "   MinIO Console:   http://localhost:9001"
echo "   PostgreSQL:      localhost:5432"
echo "   Redis:           localhost:6379"
echo ""
echo "🔑 Default Credentials:"
echo "   Jenkins:         admin / Quinton@443"
echo "   MinIO:           minioadmin / MinIO2024!SecureStorage"
echo "   PostgreSQL:      postgres / InnovationDB2024!Secure"
echo "   Redis:           (no user) / Redis2024!SecureCache"
echo ""
echo "📖 To view logs: docker-compose logs -f [service-name]"
echo "🛑 To stop all:  docker-compose down"
echo "🔄 To restart:   docker-compose restart [service-name]"
