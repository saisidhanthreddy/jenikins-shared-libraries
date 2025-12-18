#!/usr/bin/env groovy

/**
 * Push Docker image to AWS ECR
 */
def call(Map config) {
    echo "Pushing image to ECR..."
    
    script {
        // Login to ECR
        sh """
            aws ecr get-login-password --region ${env.AWS_REGION} | \
            docker login --username AWS --password-stdin ${config.ecrRegistry}
        """
        
        // Push image with tag
        sh "docker push ${env.BUILT_IMAGE}"
        
        // Push latest tag
        sh "docker push ${config.ecrRegistry}/${env.IMAGE_NAME}:latest"
        
        echo "Successfully pushed ${env.BUILT_IMAGE} to ECR"
    }
}

