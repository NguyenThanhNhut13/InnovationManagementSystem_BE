pipeline {
    agent any
    
    environment {
        DOCKER_IMAGE = 'innovation-backend'
        CONTAINER_NAME = 'innovation-backend'
    }
    
    triggers {
        githubPush()
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

        stage('Clean Docker Cache') {
            steps {
                script {
                    echo 'Cleaning Docker cache...'
                    sh 'docker builder prune -f'
                }
            }
        }
        
        stage('Build') {
            steps {
                script {
                    echo 'Building Docker image...'
                    // sh 'docker build -t ${DOCKER_IMAGE}:latest .'
                    sh 'docker build --no-cache -t ${DOCKER_IMAGE}:latest .'
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
                    echo "Waiting for application to start..."
                    sleep 60

                    echo "Checking health endpoint..."
                    sh """
                        if ! curl -f http://localhost:8081/actuator/health; then
                            echo '❌ Health check failed!'
                            exit 1
                        fi
                    """
                }
            }
        }
    }
    
    post {
        success {
            echo 'Deployment successful!'
            emailext (
                subject: "✅ Deployment SUCCESS: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    <h2>Deployment Successful!</h2>
                    <p><strong>Project:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    <p><strong>Commit:</strong> ${env.GIT_COMMIT}</p>
                    <p><strong>Branch:</strong> ${env.GIT_BRANCH}</p>
                    <p><strong>Status:</strong> <span style="color: green;">SUCCESS</span></p>
                    <hr>
                    <p>Application is now running at: <a href="http://192.168.2.32:8081">http://192.168.2.32:8081</a></p>
                """,
                to: '${DEFAULT_RECIPIENTS}',
                mimeType: 'text/html'
            )
        }
        failure {
            echo 'Deployment failed!'
            emailext (
                subject: "❌ Deployment FAILED: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    <h2>Deployment Failed!</h2>
                    <p><strong>Project:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    <p><strong>Commit:</strong> ${env.GIT_COMMIT}</p>
                    <p><strong>Branch:</strong> ${env.GIT_BRANCH}</p>
                    <p><strong>Status:</strong> <span style="color: red;">FAILED</span></p>
                    <hr>
                    <p>Please check the console output for details: <a href="${env.BUILD_URL}console">${env.BUILD_URL}console</a></p>
                """,
                to: '${DEFAULT_RECIPIENTS}',
                mimeType: 'text/html'
            )
        }
        unstable {
            echo 'Deployment unstable!'
            emailext (
                subject: "⚠️ Deployment UNSTABLE: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    <h2>Deployment Unstable!</h2>
                    <p><strong>Project:</strong> ${env.JOB_NAME}</p>
                    <p><strong>Build Number:</strong> ${env.BUILD_NUMBER}</p>
                    <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
                    <p><strong>Status:</strong> <span style="color: orange;">UNSTABLE</span></p>
                """,
                to: '${DEFAULT_RECIPIENTS}',
                mimeType: 'text/html'
            )
        }
    }
}
