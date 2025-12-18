#!/usr/bin/env groovy

/**
 * Build Docker image for Golang applications
 */
def call(Map config) {
    echo "Building Golang Docker image..."
    
    def dockerfile = config.dockerfile ?: 'Dockerfile'
    def imageName = "${config.imageName ?: config.appName}"
    def fullImageName = "${config.ecrRegistry}/${imageName}:${env.IMAGE_TAG}"
    
    // Validate Dockerfile exists
    if (!fileExists(dockerfile)) {
        error("Dockerfile not found: ${dockerfile}")
    }
    
    sh """
        docker build -f ${dockerfile} -t ${fullImageName} .
        docker tag ${fullImageName} ${config.ecrRegistry}/${imageName}:latest
    """
    
    env.BUILT_IMAGE = fullImageName
    env.IMAGE_NAME = imageName
}

