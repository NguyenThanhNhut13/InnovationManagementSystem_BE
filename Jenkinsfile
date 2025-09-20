pipeline {
    agent any
    
    environment {
        // Application Configuration
        APP_NAME = 'innovation-management-system-be'
        APP_VERSION = "${BUILD_NUMBER}"
        DOCKER_IMAGE = "${APP_NAME}:${APP_VERSION}"
        DOCKER_REGISTRY = 'localhost:5000' // Local registry or your registry
        
        // Server Configuration
        PORT = '8080'
        
        // Database Configuration (PostgreSQL)
        SPRING_DATASOURCE_URL = 'jdbc:postgresql://postgres:5432/innovation_management'
        SPRING_DATASOURCE_USERNAME = 'postgres'
        SPRING_DATASOURCE_PASSWORD = 'InnovationDB2024!Secure'
        SPRING_DATASOURCE_SSL = 'false'
        SPRING_DATASOURCE_SSLMODE = 'disable'
        
        // Redis Configuration
        SPRING_DATA_REDIS_HOST = 'redis'
        SPRING_DATA_REDIS_PORT = '6379'
        SPRING_DATA_REDIS_USERNAME = ''
        SPRING_DATA_REDIS_PASSWORD = 'Redis2024!SecureCache'
        SPRING_DATA_REDIS_DATABASE = '0'
        SPRING_DATA_REDIS_SSL = 'false'
        
        // JWT Configuration
        JWT_ACCESS_TOKEN_EXPIRATION = '3600000'
        JWT_REFRESH_TOKEN_EXPIRATION = '86400000'
        
        // Email Configuration
        SPRING_MAIL_HOST = 'smtp.gmail.com'
        SPRING_MAIL_PORT = '587'
        SPRING_MAIL_USERNAME = 'ntanhquan.sly@gmail.com'
        SPRING_MAIL_PASSWORD = 'muszahcaqthignup'
        
        // MinIO Object Storage Configuration
        MINIO_ENDPOINT = 'http://minio:9000'
        MINIO_ACCESS_KEY = 'minioadmin'
        MINIO_SECRET_KEY = 'MinIO2024!SecureStorage'
        MINIO_BUCKET_NAME = 'innovation-management'
        
        // Maven Configuration
        MAVEN_OPTS = '-Xmx1024m'
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }
    
    stages {
        stage('Checkout') {
            steps {
                echo 'Checking out source code...'
                checkout scm
                script {
                    env.GIT_COMMIT_SHORT = sh(
                        script: 'git rev-parse --short HEAD',
                        returnStdout: true
                    ).trim()
                }
            }
        }
        
        stage('Build') {
            steps {
                echo 'Building application...'
                sh '''
                    mvn clean compile \
                        -Dspring.mail.host=${SPRING_MAIL_HOST} \
                        -Dspring.mail.port=${SPRING_MAIL_PORT} \
                        -Dspring.mail.username=${SPRING_MAIL_USERNAME} \
                        -Dspring.mail.password=${SPRING_MAIL_PASSWORD} \
                        -Djwt.access.token.expiration=${JWT_ACCESS_TOKEN_EXPIRATION} \
                        -Djwt.refresh.token.expiration=${JWT_REFRESH_TOKEN_EXPIRATION}
                '''
            }
            post {
                always {
                    echo 'Build completed'
                }
            }
        }
        
        stage('Package') {
            steps {
                echo 'Building application package...'
                sh '''
                    mvn clean package -DskipTests \
                        -Dspring.profiles.active=prod \
                        -Dspring.datasource.url=${SPRING_DATASOURCE_URL} \
                        -Dspring.datasource.username=${SPRING_DATASOURCE_USERNAME} \
                        -Dspring.datasource.password=${SPRING_DATASOURCE_PASSWORD} \
                        -Dspring.datasource.ssl=${SPRING_DATASOURCE_SSL} \
                        -Dspring.datasource.sslmode=${SPRING_DATASOURCE_SSLMODE} \
                        -Dspring.data.redis.host=${SPRING_DATA_REDIS_HOST} \
                        -Dspring.data.redis.port=${SPRING_DATA_REDIS_PORT} \
                        -Dspring.data.redis.username=${SPRING_DATA_REDIS_USERNAME} \
                        -Dspring.data.redis.password=${SPRING_DATA_REDIS_PASSWORD} \
                        -Dspring.data.redis.database=${SPRING_DATA_REDIS_DATABASE} \
                        -Dspring.data.redis.ssl=${SPRING_DATA_REDIS_SSL} \
                        -Dspring.mail.host=${SPRING_MAIL_HOST} \
                        -Dspring.mail.port=${SPRING_MAIL_PORT} \
                        -Dspring.mail.username=${SPRING_MAIL_USERNAME} \
                        -Dspring.mail.password=${SPRING_MAIL_PASSWORD} \
                        -Djwt.access.token.expiration=${JWT_ACCESS_TOKEN_EXPIRATION} \
                        -Djwt.refresh.token.expiration=${JWT_REFRESH_TOKEN_EXPIRATION} \
                        -Dminio.endpoint=${MINIO_ENDPOINT} \
                        -Dminio.access-key=${MINIO_ACCESS_KEY} \
                        -Dminio.secret-key=${MINIO_SECRET_KEY} \
                        -Dminio.bucket.name=${MINIO_BUCKET_NAME}
                '''
            }
            post {
                success {
                    echo 'Package created successfully'
                }
            }
        }
        
        stage('Docker Build') {
            steps {
                echo 'Building Docker image...'
                script {
                    def image = docker.build("${DOCKER_IMAGE}")
                    // Tag as latest for local use
                    sh "docker tag ${DOCKER_IMAGE} innovation-management-system-be:latest"
                    echo "Docker image built successfully: ${DOCKER_IMAGE}"
                }
            }
        }
        
        stage('Security Scan') {
            steps {
                echo 'Running security scan...'
                sh '''
                    # Install Trivy if not present
                    if ! command -v trivy &> /dev/null; then
                        curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin
                    fi
                    
                    # Scan Docker image
                    trivy image --exit-code 0 --severity HIGH,CRITICAL ${DOCKER_IMAGE}
                '''
            }
        }
        
        stage('Deploy to Staging') {
            when {
                branch 'develop'
            }
            steps {
                echo 'Deploying to staging environment...'
                script {
                    sh '''
                        # Stop existing container
                        docker stop ${APP_NAME}-staging || true
                        docker rm ${APP_NAME}-staging || true
                        
                        # Run new container
                        docker run -d \
                            --name ${APP_NAME}-staging \
                            --network innovationmanagementsystem_be_default \
                            -p 8082:8080 \
                            -e SPRING_PROFILES_ACTIVE=staging \
                            -e SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL} \
                            -e SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME} \
                            -e SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD} \
                            -e SPRING_DATASOURCE_SSL=${SPRING_DATASOURCE_SSL} \
                            -e SPRING_DATASOURCE_SSLMODE=${SPRING_DATASOURCE_SSLMODE} \
                            -e SPRING_DATA_REDIS_HOST=${SPRING_DATA_REDIS_HOST} \
                            -e SPRING_DATA_REDIS_PORT=${SPRING_DATA_REDIS_PORT} \
                            -e SPRING_DATA_REDIS_USERNAME=${SPRING_DATA_REDIS_USERNAME} \
                            -e SPRING_DATA_REDIS_PASSWORD=${SPRING_DATA_REDIS_PASSWORD} \
                            -e SPRING_DATA_REDIS_DATABASE=${SPRING_DATA_REDIS_DATABASE} \
                            -e SPRING_DATA_REDIS_SSL=${SPRING_DATA_REDIS_SSL} \
                            -e SPRING_MAIL_HOST=${SPRING_MAIL_HOST} \
                            -e SPRING_MAIL_PORT=${SPRING_MAIL_PORT} \
                            -e SPRING_MAIL_USERNAME=${SPRING_MAIL_USERNAME} \
                            -e SPRING_MAIL_PASSWORD=${SPRING_MAIL_PASSWORD} \
                            -e JWT_ACCESS_TOKEN_EXPIRATION=${JWT_ACCESS_TOKEN_EXPIRATION} \
                            -e JWT_REFRESH_TOKEN_EXPIRATION=${JWT_REFRESH_TOKEN_EXPIRATION} \
                            -e MINIO_ENDPOINT=${MINIO_ENDPOINT} \
                            -e MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY} \
                            -e MINIO_SECRET_KEY=${MINIO_SECRET_KEY} \
                            -e MINIO_BUCKET_NAME=${MINIO_BUCKET_NAME} \
                            ${DOCKER_IMAGE}
                    '''
                }
            }
            post {
                success {
                    echo 'Staging deployment successful!'
                    // Add notification here (Slack, email, etc.)
                }
                failure {
                    echo 'Staging deployment failed!'
                    // Add notification here
                }
            }
        }
        
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                echo 'Deploying to production environment...'
                script {
                    sh '''
                        # Stop existing container
                        docker stop ${APP_NAME}-prod || true
                        docker rm ${APP_NAME}-prod || true
                        
                        # Run new container
                        docker run -d \
                            --name ${APP_NAME}-prod \
                            --network innovationmanagementsystem_be_default \
                            -p 8080:8080 \
                            -e SPRING_PROFILES_ACTIVE=prod \
                            -e SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL} \
                            -e SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME} \
                            -e SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD} \
                            -e SPRING_DATASOURCE_SSL=${SPRING_DATASOURCE_SSL} \
                            -e SPRING_DATASOURCE_SSLMODE=${SPRING_DATASOURCE_SSLMODE} \
                            -e SPRING_DATA_REDIS_HOST=${SPRING_DATA_REDIS_HOST} \
                            -e SPRING_DATA_REDIS_PORT=${SPRING_DATA_REDIS_PORT} \
                            -e SPRING_DATA_REDIS_USERNAME=${SPRING_DATA_REDIS_USERNAME} \
                            -e SPRING_DATA_REDIS_PASSWORD=${SPRING_DATA_REDIS_PASSWORD} \
                            -e SPRING_DATA_REDIS_DATABASE=${SPRING_DATA_REDIS_DATABASE} \
                            -e SPRING_DATA_REDIS_SSL=${SPRING_DATA_REDIS_SSL} \
                            -e SPRING_MAIL_HOST=${SPRING_MAIL_HOST} \
                            -e SPRING_MAIL_PORT=${SPRING_MAIL_PORT} \
                            -e SPRING_MAIL_USERNAME=${SPRING_MAIL_USERNAME} \
                            -e SPRING_MAIL_PASSWORD=${SPRING_MAIL_PASSWORD} \
                            -e JWT_ACCESS_TOKEN_EXPIRATION=${JWT_ACCESS_TOKEN_EXPIRATION} \
                            -e JWT_REFRESH_TOKEN_EXPIRATION=${JWT_REFRESH_TOKEN_EXPIRATION} \
                            -e MINIO_ENDPOINT=${MINIO_ENDPOINT} \
                            -e MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY} \
                            -e MINIO_SECRET_KEY=${MINIO_SECRET_KEY} \
                            -e MINIO_BUCKET_NAME=${MINIO_BUCKET_NAME} \
                            --restart unless-stopped \
                            ${DOCKER_IMAGE}
                    '''
                }
            }
            post {
                success {
                    echo 'Production deployment successful!'
                    // Add notification here
                }
                failure {
                    echo 'Production deployment failed!'
                    // Add notification here
                }
            }
        }
    }
    
    post {
        always {
            echo 'Pipeline execution completed!'
            cleanWs()
        }
        success {
            echo 'Pipeline executed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
        unstable {
            echo 'Pipeline is unstable!'
        }
    }
}
