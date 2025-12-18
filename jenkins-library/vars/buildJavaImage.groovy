#!/usr/bin/env groovy

/**
 * Build Docker image for Java applications
 */
def call(Map config) {
    echo "Building Java Docker image..."
    
    def dockerfile = config.dockerfile ?: 'Dockerfile'
    def imageName = "${config.imageName ?: config.appName}"
    def fullImageName = "${config.ecrRegistry}/${imageName}:${env.IMAGE_TAG}"
    
    // Validate Dockerfile exists
    if (!fileExists(dockerfile)) {
        error("Dockerfile not found: ${dockerfile}")
    }
    
    // Build Maven/Gradle project if needed
    if (fileExists('pom.xml')) {
        sh 'mvn clean package -DskipTests'
    } else if (fileExists('build.gradle')) {
        sh './gradlew build -x test'
    }
    
    sh """
        docker build -f ${dockerfile} -t ${fullImageName} .
        docker tag ${fullImageName} ${config.ecrRegistry}/${imageName}:latest
    """
    
    env.BUILT_IMAGE = fullImageName
    env.IMAGE_NAME = imageName
}

