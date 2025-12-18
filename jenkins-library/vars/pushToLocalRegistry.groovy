#!/usr/bin/env groovy

/**
 * Push Docker image to local Docker registry
 */
def call(Map config) {
    echo "Pushing image to local Docker registry..."
    
    script {
        def registryUrl = config.localRegistry ?: config.registryUrl ?: 'localhost:5000'
        
        // Login to local registry if credentials provided
        if (config.registryUsername && config.registryPassword) {
            sh """
                echo '${config.registryPassword}' | docker login ${registryUrl} -u '${config.registryUsername}' --password-stdin
            """
        }
        
        // Tag image with registry URL
        def localImageName = "${registryUrl}/${env.IMAGE_NAME}:${env.IMAGE_TAG}"
        def localLatestImage = "${registryUrl}/${env.IMAGE_NAME}:latest"
        
        sh """
            docker tag ${env.BUILT_IMAGE} ${localImageName}
            docker tag ${env.BUILT_IMAGE} ${localLatestImage}
        """
        
        // Push image with tag
        sh "docker push ${localImageName}"
        
        // Push latest tag
        sh "docker push ${localLatestImage}"
        
        // Update environment variables
        env.BUILT_IMAGE = localImageName
        env.REGISTRY_IMAGE = localImageName
        
        echo "Successfully pushed ${localImageName} to local registry"
    }
}

