pipeline {
    agent any

    tools {
        jdk 'jdk17'  
        maven 'maven3'
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
