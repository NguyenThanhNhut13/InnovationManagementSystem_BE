pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'innovation-backend'
        CONTAINER_NAME = 'innovation-backend'
    }
    
    triggers {
        // Poll GitHub mỗi 5 phút để check code mới (dùng cho local test)
        // Có thể tắt khi đã setup webhook
        pollSCM('H/5 * * * *')
    }
    
    stages {
        stage('Build') {
            steps {
                script {
                    echo 'Building Docker image...'
                    sh 'docker build -t ${DOCKER_IMAGE}:latest .'
                }
            }
        }
        
        stage('Stop Old Container') {
            steps {
                script {
                    echo 'Stopping old container...'
                    sh '''
                        docker stop ${CONTAINER_NAME} || true
                        docker rm ${CONTAINER_NAME} || true
                    '''
                }
            }
        }
        
        stage('Deploy') {
            steps {
                script {
                    echo 'Deploying new container...'
                    sh '''
                        docker run -d \
                            --name ${CONTAINER_NAME} \
                            --network innovation-management-system-be_default \
                            -p 8081:8081 \
                            -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/innovation_management \
                            -e SPRING_DATASOURCE_USERNAME=postgresADMIN \
                            -e SPRING_DATASOURCE_PASSWORD=InnovationSystemPostgresql!Secure!Quinton \
                            -e SPRING_DATA_REDIS_HOST=redis \
                            -e SPRING_DATA_REDIS_PORT=6379 \
                            -e SPRING_DATA_REDIS_PASSWORD=InnovationSystemRedis!Secure!Quinton \
                            -e MINIO_ENDPOINT=http://minio:9000 \
                            -e MINIO_ACCESS_KEY=minioADMIN \
                            -e MINIO_SECRET_KEY=InnovationSystemMinIO!Secure!Quinton \
                            -e PORT=8081 \
                            ${DOCKER_IMAGE}:latest
                    '''
                }
            }
        }
        
        stage('Health Check') {
            steps {
                script {
                    echo 'Checking application health...'
                    sh '''
                        sleep 30
                        curl -f http://localhost:8081/actuator/health || echo "Health check endpoint not available"
                    '''
                }
            }
        }
    }
    
    post {
        success {
            echo 'Deployment successful!'
        }
        failure {
            echo 'Deployment failed!'
        }
    }
}
