#!/bin/bash

# Innovation Management System - Management Script
# Quick commands for managing the application

set -e

GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

print_info() {
    echo -e "${YELLOW}$1${NC}"
}

print_success() {
    echo -e "${GREEN}$1${NC}"
}

show_help() {
    echo "Innovation Management System - Management Script"
    echo ""
    echo "Usage: ./manage.sh [command]"
    echo ""
    echo "Commands:"
    echo "  start       - Start all services"
    echo "  stop        - Stop all services"
    echo "  restart     - Restart all services"
    echo "  status      - Show services status"
    echo "  logs        - Show logs (all services)"
    echo "  logs-app    - Show application logs"
    echo "  logs-db     - Show database logs"
    echo "  logs-jenkins - Show Jenkins logs"
    echo "  backup-db   - Backup database"
    echo "  clean       - Clean up Docker resources"
    echo "  update      - Pull latest code and rebuild"
    echo "  health      - Check health of all services"
    echo "  help        - Show this help message"
}

start_services() {
    print_info "Starting services..."
    docker compose up -d
    print_success "Services started!"
}

stop_services() {
    print_info "Stopping services..."
    docker compose down
    print_success "Services stopped!"
}

restart_services() {
    print_info "Restarting services..."
    docker compose restart
    print_success "Services restarted!"
}

show_status() {
    print_info "Services status:"
    docker compose ps
}

show_logs() {
    docker compose logs -f
}

show_app_logs() {
    docker logs -f innovation-backend
}

show_db_logs() {
    docker logs -f postgres
}

show_jenkins_logs() {
    docker logs -f jenkins
}

backup_database() {
    BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
    print_info "Backing up database to $BACKUP_FILE..."
    docker exec postgres pg_dump -U postgresADMIN innovation_management > "$BACKUP_FILE"
    print_success "Database backed up to $BACKUP_FILE"
}

clean_docker() {
    print_info "Cleaning up Docker resources..."
    read -p "This will remove unused containers, networks, images. Continue? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        docker system prune -a
        print_success "Cleanup completed!"
    fi
}

update_app() {
    print_info "Updating application..."
    git pull
    docker compose down
    docker compose up -d --build
    print_success "Application updated!"
}

check_health() {
    print_info "Checking health of services..."
    echo ""
    
    # PostgreSQL
    if docker exec postgres pg_isready -U postgresADMIN >/dev/null 2>&1; then
        print_success "PostgreSQL: Healthy"
    else
        echo "PostgreSQL: Unhealthy"
    fi
    
    # Redis
    if docker exec redis redis-cli ping >/dev/null 2>&1; then
        print_success "Redis: Healthy"
    else
        echo "Redis: Unhealthy"
    fi
    
    # MinIO
    if curl -f http://localhost:9000/minio/health/live >/dev/null 2>&1; then
        print_success "MinIO: Healthy"
    else
        echo "MinIO: Unhealthy"
    fi
    
    # Application
    if curl -f http://localhost:8081/actuator/health >/dev/null 2>&1; then
        print_success "Application: Healthy"
    else
        echo "Application: Unhealthy"
    fi
    
    # Jenkins
    if curl -f http://localhost:8080 >/dev/null 2>&1; then
        print_success "Jenkins: Healthy"
    else
        echo "Jenkins: Unhealthy"
    fi
}

# Main script
case "$1" in
    start)
        start_services
        ;;
    stop)
        stop_services
        ;;
    restart)
        restart_services
        ;;
    status)
        show_status
        ;;
    logs)
        show_logs
        ;;
    logs-app)
        show_app_logs
        ;;
    logs-db)
        show_db_logs
        ;;
    logs-jenkins)
        show_jenkins_logs
        ;;
    backup-db)
        backup_database
        ;;
    clean)
        clean_docker
        ;;
    update)
        update_app
        ;;
    health)
        check_health
        ;;
    help|--help|-h)
        show_help
        ;;
    *)
        echo "Unknown command: $1"
        echo ""
        show_help
        exit 1
        ;;
esac
