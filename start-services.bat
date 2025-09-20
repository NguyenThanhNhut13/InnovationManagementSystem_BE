@echo off
REM Simple Jenkins CI/CD Startup Script

echo 🚀 Starting Innovation Management System with Jenkins CI/CD...

REM Check Docker
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Docker is not running. Please start Docker Desktop first.
    pause
    exit /b 1
)

REM Start services
echo 🐳 Starting all services...
docker-compose up -d

REM Wait a bit for services to start
echo ⏳ Waiting for services to start...
timeout /t 30 /nobreak >nul

echo.
echo 🎉 Services are starting!
echo ==================================================
echo 📋 Service URLs:
echo    Backend API:     http://localhost:8080
echo    Jenkins:         http://localhost:8081
echo    MinIO Console:   http://localhost:9001
echo.
echo 🔑 Jenkins Login: admin / admin123
echo.
echo 📖 Commands:
echo    View logs: docker-compose logs -f
echo    Stop all:  docker-compose down
echo.
pause
