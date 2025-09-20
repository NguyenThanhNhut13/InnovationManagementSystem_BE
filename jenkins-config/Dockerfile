# Simple Jenkins Dockerfile
FROM jenkins/jenkins:lts-jdk17

# Switch to root to install packages
USER root

# Install essential tools
RUN apt-get update && \
    apt-get install -y docker.io maven curl git && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Install plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Copy configuration
COPY jenkins.yaml /usr/share/jenkins/ref/casc/jenkins.yaml

# Switch back to jenkins user
USER jenkins

# Set environment variables
ENV CASC_JENKINS_CONFIG=/var/jenkins_home/casc_configs/jenkins.yaml
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
