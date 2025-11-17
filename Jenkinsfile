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
        stage('Prepare Network') {
            steps {
                script {
                    echo 'Ensuring Docker network exists...'
                    sh '''
                        docker network inspect innovation-network >/dev/null 2>&1 || \
                        docker network create innovation-network
                    '''
                }
            }
        }
        
        stage('Prepare Environment') {
            steps {
                script {
                    echo 'Creating .env file and keys from Jenkins credentials...'
                    withCredentials([
                        file(credentialsId: 'env-file', variable: 'ENV_FILE'),
                        file(credentialsId: 'public-key', variable: 'PUBLIC_KEY'),
                        file(credentialsId: 'private-key', variable: 'PRIVATE_KEY')
                    ]) {
                        sh '''
                            cp $ENV_FILE .env
                            mkdir -p src/main/resources/keys
                            cp $PUBLIC_KEY src/main/resources/keys/public_key.pem
                            cp $PRIVATE_KEY src/main/resources/keys/private_key.pem
                        '''
                    }
                }
            }
        }
        
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
                            --network innovation-network \
                            -p 8081:8081 \
                            --env-file .env \
                            -e SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/innovation_management \
                            -e SPRING_DATA_REDIS_HOST=redis \
                            -e MINIO_ENDPOINT=http://minio:9000 \
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
