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
        stage('Checkout') {
            steps {
                checkout scm
                sh 'git rev-parse HEAD'
            }
        }

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
                        if ! curl -f https://api.silenthero.xyz/api/v1/utils/ping; then
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
                    <html>
                    <head>
                        <style>
                            body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f6f9; padding: 20px; margin: 0; }
                            .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
                            .header { background: linear-gradient(135deg, #28a745, #20c997); padding: 30px; text-align: center; }
                            .header h1 { color: white; margin: 0; font-size: 24px; }
                            .header .icon { font-size: 48px; margin-bottom: 10px; }
                            .content { padding: 30px; }
                            .info-card { background: #f8f9fa; border-left: 4px solid #28a745; padding: 15px; margin: 15px 0; border-radius: 0 8px 8px 0; }
                            .info-card p { margin: 8px 0; color: #333; }
                            .info-card strong { color: #28a745; }
                            .status-badge { display: inline-block; background: #28a745; color: white; padding: 8px 20px; border-radius: 20px; font-weight: bold; }
                            .btn { display: inline-block; background: #28a745; color: white; padding: 12px 30px; text-decoration: none; border-radius: 8px; margin: 10px 5px; font-weight: bold; }
                            .btn:hover { background: #218838; }
                            .footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 12px; }
                            a { color: #28a745; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <div class="icon">🚀</div>
                                <h1>Deployment Successful!</h1>
                            </div>
                            <div class="content">
                                <p>Xin chào,</p>
                                <p>Ứng dụng <strong>${env.JOB_NAME}</strong> đã được deploy thành công!</p>
                                
                                <div class="info-card">
                                    <p><strong>📦 Project:</strong> ${env.JOB_NAME}</p>
                                    <p><strong>🔢 Build Number:</strong> #${env.BUILD_NUMBER}</p>
                                    <p><strong>🌿 Branch:</strong> ${env.GIT_BRANCH}</p>
                                    <p><strong>📝 Commit:</strong> ${env.GIT_COMMIT}</p>
                                    <p><strong>📊 Status:</strong> <span class="status-badge">SUCCESS</span></p>
                                </div>
                                
                                <div style="text-align: center; margin: 25px 0;">
                                    <a href="${env.BUILD_URL}" class="btn">📋 View Build Log</a>
                                    <a href="https://api.silenthero.xyz" class="btn" style="background: #17a2b8;">🌐 Open Application</a>
                                </div>
                            </div>
                            <div class="footer">
                                <p>🤖 Jenkins CI/CD Pipeline - Innovation Management System</p>
                                <p>Build Time: \${new Date().format('dd/MM/yyyy HH:mm:ss')}</p>
                            </div>
                        </div>
                    </body>
                    </html>
                """,
                to: 'ntanhquan.slly@gmail.com',
                mimeType: 'text/html'
            )
        }
        failure {
            echo 'Deployment failed!'
            emailext (
                subject: "❌ Deployment FAILED: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    <html>
                    <head>
                        <style>
                            body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f6f9; padding: 20px; margin: 0; }
                            .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
                            .header { background: linear-gradient(135deg, #dc3545, #c82333); padding: 30px; text-align: center; }
                            .header h1 { color: white; margin: 0; font-size: 24px; }
                            .header .icon { font-size: 48px; margin-bottom: 10px; }
                            .content { padding: 30px; }
                            .info-card { background: #fff5f5; border-left: 4px solid #dc3545; padding: 15px; margin: 15px 0; border-radius: 0 8px 8px 0; }
                            .info-card p { margin: 8px 0; color: #333; }
                            .info-card strong { color: #dc3545; }
                            .status-badge { display: inline-block; background: #dc3545; color: white; padding: 8px 20px; border-radius: 20px; font-weight: bold; }
                            .btn { display: inline-block; background: #dc3545; color: white; padding: 12px 30px; text-decoration: none; border-radius: 8px; margin: 10px 5px; font-weight: bold; }
                            .warning-box { background: #fff3cd; border: 1px solid #ffc107; border-radius: 8px; padding: 15px; margin: 20px 0; }
                            .footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 12px; }
                            a { color: #dc3545; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <div class="icon">⚠️</div>
                                <h1>Deployment Failed!</h1>
                            </div>
                            <div class="content">
                                <p>Xin chào,</p>
                                <p>Deployment cho <strong>${env.JOB_NAME}</strong> đã thất bại. Vui lòng kiểm tra logs để biết chi tiết.</p>
                                
                                <div class="info-card">
                                    <p><strong>📦 Project:</strong> ${env.JOB_NAME}</p>
                                    <p><strong>🔢 Build Number:</strong> #${env.BUILD_NUMBER}</p>
                                    <p><strong>🌿 Branch:</strong> ${env.GIT_BRANCH}</p>
                                    <p><strong>📝 Commit:</strong> ${env.GIT_COMMIT}</p>
                                    <p><strong>📊 Status:</strong> <span class="status-badge">FAILED</span></p>
                                </div>
                                
                                <div class="warning-box">
                                    <p><strong>⚡ Hành động cần thực hiện:</strong></p>
                                    <ul>
                                        <li>Kiểm tra console output để xem lỗi chi tiết</li>
                                        <li>Kiểm tra Docker logs nếu container không khởi động được</li>
                                        <li>Xác minh các biến môi trường và credentials</li>
                                    </ul>
                                </div>
                                
                                <div style="text-align: center; margin: 25px 0;">
                                    <a href="${env.BUILD_URL}console" class="btn">🔍 View Console Output</a>
                                </div>
                            </div>
                            <div class="footer">
                                <p>🤖 Jenkins CI/CD Pipeline - Innovation Management System</p>
                                <p>Build Time: \${new Date().format('dd/MM/yyyy HH:mm:ss')}</p>
                            </div>
                        </div>
                    </body>
                    </html>
                """,
                to: 'ntanhquan.slly@gmail.com',
                mimeType: 'text/html'
            )
        }
        unstable {
            echo 'Deployment unstable!'
            emailext (
                subject: "⚠️ Deployment UNSTABLE: ${env.JOB_NAME} - Build #${env.BUILD_NUMBER}",
                body: """
                    <html>
                    <head>
                        <style>
                            body { font-family: 'Segoe UI', Arial, sans-serif; background-color: #f4f6f9; padding: 20px; margin: 0; }
                            .container { max-width: 600px; margin: 0 auto; background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
                            .header { background: linear-gradient(135deg, #ffc107, #fd7e14); padding: 30px; text-align: center; }
                            .header h1 { color: white; margin: 0; font-size: 24px; }
                            .header .icon { font-size: 48px; margin-bottom: 10px; }
                            .content { padding: 30px; }
                            .info-card { background: #fff8e1; border-left: 4px solid #ffc107; padding: 15px; margin: 15px 0; border-radius: 0 8px 8px 0; }
                            .info-card p { margin: 8px 0; color: #333; }
                            .info-card strong { color: #fd7e14; }
                            .status-badge { display: inline-block; background: #ffc107; color: #333; padding: 8px 20px; border-radius: 20px; font-weight: bold; }
                            .btn { display: inline-block; background: #ffc107; color: #333; padding: 12px 30px; text-decoration: none; border-radius: 8px; margin: 10px 5px; font-weight: bold; }
                            .footer { background: #f8f9fa; padding: 20px; text-align: center; color: #666; font-size: 12px; }
                            a { color: #fd7e14; }
                        </style>
                    </head>
                    <body>
                        <div class="container">
                            <div class="header">
                                <div class="icon">⚡</div>
                                <h1>Deployment Unstable</h1>
                            </div>
                            <div class="content">
                                <p>Xin chào,</p>
                                <p>Deployment cho <strong>${env.JOB_NAME}</strong> không ổn định. Có thể có một số vấn đề cần kiểm tra.</p>
                                
                                <div class="info-card">
                                    <p><strong>📦 Project:</strong> ${env.JOB_NAME}</p>
                                    <p><strong>🔢 Build Number:</strong> #${env.BUILD_NUMBER}</p>
                                    <p><strong>📊 Status:</strong> <span class="status-badge">UNSTABLE</span></p>
                                </div>
                                
                                <div style="text-align: center; margin: 25px 0;">
                                    <a href="${env.BUILD_URL}" class="btn">📋 View Build Details</a>
                                </div>
                            </div>
                            <div class="footer">
                                <p>🤖 Jenkins CI/CD Pipeline - Innovation Management System</p>
                            </div>
                        </div>
                    </body>
                    </html>
                """,
                to: 'ntanhquan.slly@gmail.com',
                mimeType: 'text/html'
            )
        }
    }
}
