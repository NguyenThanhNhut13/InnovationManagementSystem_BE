#!/bin/bash

# Jenkins Plugin Installation Script
# This script installs all required plugins for the Innovation Management System

set -e

echo "Installing Jenkins plugins..."

# Wait for Jenkins to be ready
while ! curl -f http://localhost:8080/login >/dev/null 2>&1; do
    echo "Waiting for Jenkins to start..."
    sleep 5
done

echo "Jenkins is ready. Installing plugins..."

# Install plugins from plugins.txt
jenkins-plugin-cli --plugin-file /var/jenkins_home/casc_configs/plugins.txt

echo "All plugins installed successfully!"
