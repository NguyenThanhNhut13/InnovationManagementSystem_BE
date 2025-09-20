pipeline {
    agent any
    
    environment {
        // Application Configuration
        APP_NAME = 'innovation-management-system-be'
        APP_VERSION = "${BUILD_NUMBER}"
        DOCKER_IMAGE = "${APP_NAME}:${APP_VERSION}"
        DOCKER_REGISTRY = 'localhost:5000' // Local registry or your registry
        
        // Database Configuration
        DB_HOST = 'postgres'
        DB_PORT = '5432'
        DB_NAME = 'innovation_management'
        DB_USER = 'postgres'
        DB_PASSWORD = 'InnovationDB2024!Secure'
        
        // Redis Configuration
        REDIS_HOST = 'redis'
        REDIS_PORT = '6379'
        REDIS_PASSWORD = 'Redis2024!SecureCache'
        
        // MinIO Configuration
        MINIO_ENDPOINT = 'http://minio:9000'
        MINIO_ACCESS_KEY = 'minioadmin'
        MINIO_SECRET_KEY = 'MinIO2024!SecureStorage'
        
        // Maven Configuration
        MAVEN_OPTS = '-Xmx1024m -XX:MaxPermSize=256m'
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
        
        stage('Build & Test') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        echo 'Running unit tests...'
                        sh '''
                            mvn clean test \
                                -Dspring.profiles.active=test \
                                -Dspring.datasource.url=jdbc:h2:mem:testdb \
                                -Dspring.jpa.hibernate.ddl-auto=create-drop
                        '''
                    }
                    post {
                        always {
                            echo 'Unit tests completed'
                        }
                    }
                }
                
                stage('Code Quality') {
                    steps {
                        echo 'Running code quality checks...'
                        sh '''
                            mvn clean compile \
                                checkstyle:check \
                                pmd:check \
                                spotbugs:check
                        '''
                    }
                    post {
                        always {
                            echo 'Code quality checks completed'
                        }
                    }
                }
            }
        }
        
        stage('Package') {
            steps {
                echo 'Building application package...'
                sh '''
                    mvn clean package -DskipTests \
                        -Dspring.profiles.active=prod \
                        -Dspring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME} \
                        -Dspring.datasource.username=${DB_USER} \
                        -Dspring.datasource.password=${DB_PASSWORD} \
                        -Dspring.data.redis.host=${REDIS_HOST} \
                        -Dspring.data.redis.port=${REDIS_PORT} \
                        -Dspring.data.redis.password=${REDIS_PASSWORD} \
                        -Dminio.endpoint=${MINIO_ENDPOINT} \
                        -Dminio.access-key=${MINIO_ACCESS_KEY} \
                        -Dminio.secret-key=${MINIO_SECRET_KEY}
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
                    docker.withRegistry("http://${DOCKER_REGISTRY}") {
                        image.push()
                        image.push("latest")
                    }
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
                            -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/innovation_management \
                            -e SPRING_DATASOURCE_USERNAME=${DB_USER} \
                            -e SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD} \
                            -e SPRING_DATA_REDIS_HOST=${REDIS_HOST} \
                            -e SPRING_DATA_REDIS_PORT=${REDIS_PORT} \
                            -e SPRING_DATA_REDIS_PASSWORD=${REDIS_PASSWORD} \
                            -e MINIO_ENDPOINT=${MINIO_ENDPOINT} \
                            -e MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY} \
                            -e MINIO_SECRET_KEY=${MINIO_SECRET_KEY} \
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
                            -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/innovation_management \
                            -e SPRING_DATASOURCE_USERNAME=${DB_USER} \
                            -e SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD} \
                            -e SPRING_DATA_REDIS_HOST=${REDIS_HOST} \
                            -e SPRING_DATA_REDIS_PORT=${REDIS_PORT} \
                            -e SPRING_DATA_REDIS_PASSWORD=${REDIS_PASSWORD} \
                            -e MINIO_ENDPOINT=${MINIO_ENDPOINT} \
                            -e MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY} \
                            -e MINIO_SECRET_KEY=${MINIO_SECRET_KEY} \
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
