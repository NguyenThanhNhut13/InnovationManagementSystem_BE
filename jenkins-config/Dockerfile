# Jenkins Dockerfile with pre-installed plugins
FROM jenkins/jenkins:lts-jdk17

# Switch to root to install additional packages
USER root

# Install Docker CLI, Maven, and other tools
RUN apt-get update && \
    apt-get install -y \
        docker.io \
        maven \
        curl \
        wget \
        git && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

# Install Jenkins plugin CLI
RUN jenkins-plugin-cli --version

# Copy plugin list and install plugins
COPY plugins.txt /usr/share/jenkins/ref/plugins.txt
RUN jenkins-plugin-cli --plugin-file /usr/share/jenkins/ref/plugins.txt

# Copy configuration files
COPY jenkins.yaml /usr/share/jenkins/ref/casc/jenkins.yaml

# Switch back to jenkins user
USER jenkins

# Set environment variables
ENV CASC_JENKINS_CONFIG=/var/jenkins_home/casc_configs/jenkins.yaml
ENV JAVA_OPTS="-Djenkins.install.runSetupWizard=false"
ENV MAVEN_HOME=/usr/share/maven
ENV PATH=$PATH:$MAVEN_HOME/bin
