# Jenkins CI/CD Configuration

This directory contains the Jenkins configuration files for the Innovation Management System CI/CD pipeline.

## Files Description

### `jenkins.yaml`
Main Jenkins configuration file using Configuration as Code (CasC) plugin. Contains:
- System settings and security configuration
- Tool installations (Git, Maven, JDK)
- Global libraries and environment variables

### `plugins.txt`
List of Jenkins plugins required for the CI/CD pipeline:
- **Pipeline plugins**: Core workflow functionality
- **Docker plugins**: Container build and deployment
- **Git plugins**: Source code management
- **Maven plugins**: Java build automation
- **Quality plugins**: Code analysis and testing
- **Notification plugins**: Build notifications

### `Dockerfile`
Custom Jenkins Docker image with:
- Pre-installed plugins
- Docker CLI for container operations
- Configuration as Code setup

### `install-plugins.sh`
Script to install Jenkins plugins after container startup.

### `job-config.xml`
Jenkins job configuration template for the Innovation Management System pipeline.

## Setup Instructions

1. **Start Jenkins with Docker Compose**:
   ```bash
   docker-compose up -d jenkins
   ```

2. **Access Jenkins**:
   - URL: http://localhost:8081
   - Username: admin
   - Password: Quinton@443

3. **Create New Pipeline Job**:
   - Go to "New Item" → "Pipeline"
   - Name: "Innovation-Management-System"
   - Pipeline script from SCM: Git
   - Repository URL: `https://github.com/NguyenThanhNhut13/InnovationManagementSystem_BE.git`
   - Script Path: Jenkinsfile

4. **Configure Credentials** (REQUIRED for private repository):
   - Go to "Manage Jenkins" → "Manage Credentials"
   - Add GitHub credentials for private repository access
   - See `setup-credentials.md` for detailed instructions
   - Add Docker registry credentials if using private registry

5. **Setup Auto-Deploy** (Optional but recommended):
   - See `github-webhook-setup.md` for GitHub webhook configuration
   - This enables instant deployment when pushing to main branch

## Pipeline Features

The CI/CD pipeline includes:

### Build Stages
- **Checkout**: Source code retrieval
- **Unit Tests**: Maven test execution with coverage
- **Code Quality**: Checkstyle, PMD, SpotBugs analysis
- **Package**: JAR file creation
- **Docker Build**: Container image creation
- **Security Scan**: Trivy vulnerability scanning

### Deployment Stages
- **Staging**: Automatic deployment to staging environment (develop branch)
- **Production**: Automatic deployment to production (main branch) - **Auto-deploy khi push main**

### Quality Gates
- Test coverage reporting
- Code quality metrics
- Security vulnerability scanning
- Build artifact archiving

## Environment Variables

The pipeline uses the following environment variables:
- `APP_NAME`: Application name
- `APP_VERSION`: Build version
- `DOCKER_IMAGE`: Docker image name and tag
- Database connection settings
- Redis connection settings
- MinIO storage settings

## Customization

To customize the pipeline:

1. **Modify Jenkinsfile**: Update pipeline stages and configurations
2. **Update plugins.txt**: Add or remove required plugins
3. **Modify jenkins.yaml**: Update Jenkins system configuration
4. **Environment variables**: Adjust in Jenkinsfile environment section

## Troubleshooting

### Common Issues

1. **Jenkins not starting**:
   - Check Docker logs: `docker logs jenkins`
   - Verify port 8081 is not in use
   - Check Jenkins data volume permissions

2. **Plugin installation fails**:
   - Verify internet connectivity
   - Check plugin compatibility
   - Review Jenkins logs for errors

3. **Pipeline fails**:
   - Check build logs in Jenkins UI
   - Verify environment variables
   - Ensure all required tools are installed

### Logs
- Jenkins logs: `docker logs jenkins`
- Pipeline logs: Available in Jenkins UI under each build

## Security Notes

- Change default admin password after first login
- Use strong passwords for all credentials
- Regularly update Jenkins and plugins
- Review and audit pipeline permissions
- Use HTTPS in production environments
