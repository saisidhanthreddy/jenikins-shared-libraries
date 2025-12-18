#!/usr/bin/env groovy

/**
 * Deploy application as Docker container
 */
def call(Map config) {
    echo "Deploying to Docker container..."
    
    script {
        def envConfig = loadEnvironmentConfig(config.environment)
        def containerName = config.containerName ?: "${config.appName}-${config.environment}"
        def registryUrl = config.localRegistry ?: config.registryUrl ?: 'localhost:5000'
        def imageName = "${registryUrl}/${env.IMAGE_NAME}:${env.IMAGE_TAG}"
        
        // Stop and remove existing container if it exists
        sh """
            docker stop ${containerName} || true
            docker rm ${containerName} || true
        """
        
        // Build docker run command
        def dockerRunCmd = buildDockerRunCommand(config, envConfig, containerName, imageName)
        
        // Run container
        sh dockerRunCmd
        
        // Wait for container to be healthy
        waitForContainerHealth(containerName, envConfig)
        
        echo "Successfully deployed ${config.appName} as container ${containerName}"
    }
}

def buildDockerRunCommand(Map config, Map envConfig, String containerName, String imageName) {
    def cmd = ["docker run -d"]
    
    // Container name
    cmd << "--name ${containerName}"
    
    // Restart policy
    def restartPolicy = envConfig.restartPolicy ?: config.restartPolicy ?: 'unless-stopped'
    cmd << "--restart ${restartPolicy}"
    
    // Port mapping
    def hostPort = envConfig.hostPort ?: config.hostPort ?: envConfig.port ?: 8080
    def containerPort = envConfig.port ?: config.port ?: 8080
    cmd << "-p ${hostPort}:${containerPort}"
    
    // Environment variables
    if (config.environmentVars) {
        config.environmentVars.each { key, value ->
            cmd << "-e ${key}=${value}"
        }
    }
    
    // Add environment from config
    cmd << "-e ENVIRONMENT=${config.environment}"
    
    // Memory limits
    if (envConfig.memoryLimit) {
        cmd << "--memory=${envConfig.memoryLimit}"
    }
    
    // CPU limits
    if (envConfig.cpuLimit) {
        cmd << "--cpus=${envConfig.cpuLimit.replaceAll(/[^0-9.]/, '')}"
    }
    
    // Network
    if (config.network) {
        cmd << "--network ${config.network}"
    }
    
    // Volume mounts
    if (config.volumes) {
        config.volumes.each { volume ->
            cmd << "-v ${volume}"
        }
    }
    
    // Image name
    cmd << imageName
    
    // Command override
    if (config.command) {
        cmd << config.command
    }
    
    return cmd.join(' ')
}

def waitForContainerHealth(String containerName, Map envConfig) {
    def maxAttempts = envConfig.healthCheckMaxAttempts ?: 30
    def interval = envConfig.healthCheckInterval ?: 5
    
    for (int i = 0; i < maxAttempts; i++) {
        def status = sh(script: "docker inspect -f '{{.State.Status}}' ${containerName}", returnStdout: true).trim()
        
        if (status == 'running') {
            echo "Container ${containerName} is running"
            return
        }
        
        sleep(interval)
    }
    
    error("Container ${containerName} failed to start within timeout")
}

