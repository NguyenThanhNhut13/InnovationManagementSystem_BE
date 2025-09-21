pipeline {
    agent {
        docker {
            image 'maven:3.9.6-eclipse-temurin-17'
            args '-v /root/.m2:/root/.m2'
        }
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/NguyenThanhNhut13/InnovationManagementSystem_BE'
            }
        }

        stage('Build') {
            steps {
                sh 'mvn clean package -DskipTests'
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t innovation-backend:latest .'
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker stop backend || true && docker rm backend || true'
                sh 'docker run -d --name backend --network=host innovation-backend:latest'
            }
        }
    }
}
