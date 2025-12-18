#!/usr/bin/env groovy

/**
 * Deploy application to EKS cluster
 */
def call(Map config) {
    echo "Deploying to EKS cluster..."
    
    script {
        def envConfig = loadEnvironmentConfig(config.environment)
        def namespace = envConfig.namespace ?: config.namespace ?: 'default'
        def clusterName = envConfig.clusterName ?: config.clusterName ?: error("clusterName must be specified in config or environment file")
        
        // Update kubeconfig
        sh """
            aws eks update-kubeconfig --region ${env.AWS_REGION} --name ${clusterName}
        """
        
        // Apply Kubernetes manifests
        if (fileExists('k8s/deployment.yaml')) {
            sh """
                sed -i.bak 's|IMAGE_PLACEHOLDER|${env.BUILT_IMAGE}|g' k8s/deployment.yaml
                kubectl apply -f k8s/deployment.yaml -n ${namespace}
                rm -f k8s/deployment.yaml.bak || true
            """
        } else {
            // Create deployment from template
            createK8sDeployment(config, envConfig, namespace)
        }
        
        // Apply service if exists
        if (fileExists('k8s/service.yaml')) {
            sh "kubectl apply -f k8s/service.yaml -n ${namespace}"
        }
        
        // Apply ingress if exists
        if (fileExists('k8s/ingress.yaml')) {
            sh "kubectl apply -f k8s/ingress.yaml -n ${namespace}"
        }
        
        // Wait for rollout
        sh """
            kubectl rollout status deployment/${config.appName} -n ${namespace} --timeout=300s
        """
        
        echo "Successfully deployed ${config.appName} to ${namespace} namespace"
    }
}

def createK8sDeployment(Map config, Map envConfig, String namespace) {
    def deploymentYaml = """
apiVersion: apps/v1
kind: Deployment
metadata:
  name: ${config.appName}
  namespace: ${namespace}
spec:
  replicas: ${envConfig.replicas ?: 2}
  selector:
    matchLabels:
      app: ${config.appName}
  template:
    metadata:
      labels:
        app: ${config.appName}
    spec:
      containers:
      - name: ${config.appName}
        image: ${env.BUILT_IMAGE}
        ports:
        - containerPort: ${envConfig.port ?: 8080}
        env:
        - name: ENVIRONMENT
          value: "${config.environment}"
        resources:
          requests:
            memory: "${envConfig.memoryRequest ?: '256Mi'}"
            cpu: "${envConfig.cpuRequest ?: '100m'}"
          limits:
            memory: "${envConfig.memoryLimit ?: '512Mi'}"
            cpu: "${envConfig.cpuLimit ?: '500m'}"
"""
    
    writeFile file: 'k8s-deployment-temp.yaml', text: deploymentYaml
    sh "kubectl apply -f k8s-deployment-temp.yaml"
    sh "rm -f k8s-deployment-temp.yaml"
}

