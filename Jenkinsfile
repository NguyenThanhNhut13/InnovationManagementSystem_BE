pipeline {
    agent any
    
    environment {
        APP_NAME = 'innovation-management-system-be'
        DOCKER_IMAGE = "${APP_NAME}:latest"
    }
    
    options {
        buildDiscarder(logRotator(numToKeepStr: '10'))
        timeout(time: 30, unit: 'MINUTES')
        timestamps()
    }
    
    stages {
        stage('Build & Deploy') {
            steps {
                echo 'Building and deploying application...'
                script {
                    sh '''
                        # Build Docker image
                        docker build -t ${DOCKER_IMAGE} .
                        
                        # Stop and remove existing backend container
                        docker stop backend || true
                        docker rm backend || true
                        
                        # Rebuild backend service with new image
                        docker-compose up -d --build backend
                        
                        # Wait for backend to be ready
                        echo "Waiting for backend to be ready..."
                        timeout 60 bash -c 'until docker exec backend curl -f http://localhost:8080/api/v1/utils/ping; do sleep 5; done'
                        
                        echo "Deployment completed successfully!"
                    '''
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
