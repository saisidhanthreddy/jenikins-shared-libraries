# Jenkins Shared Library for Multi-Language Build and Deploy

This Jenkins shared library provides reusable pipeline steps for building Docker images and deploying applications to EKS clusters.

## Supported Languages

- **Python** - Python 3.11 applications
- **Java** - Maven/Gradle based Java applications
- **Node.js** - Node.js 18 applications
- **Golang** - Go 1.21 applications

## Directory Structure

```
jenkins-library/
├── vars/                    # Pipeline step definitions
│   ├── buildAndDeploy.groovy
│   ├── buildPythonImage.groovy
│   ├── buildJavaImage.groovy
│   ├── buildNodeImage.groovy
│   ├── buildGolangImage.groovy
│   ├── pushToECR.groovy
│   ├── deployToEKS.groovy
│   ├── loadEnvironmentConfig.groovy
│   └── validateConfig.groovy
├── src/                     # Groovy classes
│   └── org/jenkins/
│       └── DeploymentUtils.groovy
├── resources/               # Resource files
│   ├── Dockerfile.python
│   ├── Dockerfile.java
│   ├── Dockerfile.node
│   └── Dockerfile.golang
└── environments/            # Environment configurations
    ├── dev.groovy
    ├── qa.groovy
    ├── staging.groovy
    └── prod.groovy
```

## Usage

### In Jenkinsfile

```groovy
@Library('jenkins-shared-library') _

buildAndDeploy([
    language: 'python',              // Required: python, java, node, golang
    appName: 'my-app',               // Required: Application name
    environment: 'dev',              // Required: dev, staging, prod
    ecrRegistry: '123456789012.dkr.ecr.us-east-1.amazonaws.com',  // Required
    clusterName: 'dev-eks-cluster',  // Required
    awsRegion: 'us-east-1',          // Optional: defaults to us-east-1
    imageName: 'my-app',              // Optional: defaults to appName
    dockerfile: 'Dockerfile',         // Optional: defaults to Dockerfile
    namespace: 'custom-namespace',    // Optional: uses env config if not provided
    kubeconfigCredentialId: 'kubeconfig'  // Optional: credential ID for kubeconfig
])
```

### Example Jenkinsfiles

#### Python Application
```groovy
@Library('jenkins-shared-library') _

buildAndDeploy([
    language: 'python',
    appName: 'python-api',
    environment: 'dev',
    ecrRegistry: '123456789012.dkr.ecr.us-east-1.amazonaws.com',
    clusterName: 'dev-eks-cluster'
])
```

#### Java Application
```groovy
@Library('jenkins-shared-library') _

buildAndDeploy([
    language: 'java',
    appName: 'java-service',
    environment: 'staging',
    ecrRegistry: '123456789012.dkr.ecr.us-east-1.amazonaws.com',
    clusterName: 'staging-eks-cluster'
])
```

#### Node.js Application
```groovy
@Library('jenkins-shared-library') _

buildAndDeploy([
    language: 'node',
    appName: 'node-api',
    environment: 'prod',
    ecrRegistry: '123456789012.dkr.ecr.us-east-1.amazonaws.com',
    clusterName: 'prod-eks-cluster'
])
```

#### Golang Application
```groovy
@Library('jenkins-shared-library') _

buildAndDeploy([
    language: 'golang',
    appName: 'go-service',
    environment: 'dev',
    ecrRegistry: '123456789012.dkr.ecr.us-east-1.amazonaws.com',
    clusterName: 'dev-eks-cluster'
])
```

## Environment Configuration

Environment-specific configurations are stored in `environments/` directory. Each environment file should return a map with:

- `namespace` - Kubernetes namespace
- `clusterName` - EKS cluster name
- `replicas` - Number of replicas
- `port` - Container port
- `memoryRequest` - Memory request
- `cpuRequest` - CPU request
- `memoryLimit` - Memory limit
- `cpuLimit` - CPU limit
- `awsRegion` - AWS region
- `ecrRegistry` - ECR registry URL

## Prerequisites

1. **Jenkins Configuration**
   - Configure the shared library in Jenkins: Manage Jenkins → Configure System → Global Pipeline Libraries
   - Name: `jenkins-shared-library`
   - Default version: `main` or your branch name

2. **AWS Credentials**
   - AWS credentials configured in Jenkins (via AWS credentials plugin or IAM role)
   - ECR access permissions
   - EKS cluster access permissions

3. **Kubernetes Configuration**
   - Kubeconfig stored as Jenkins credential (ID: `kubeconfig` or custom)
   - kubectl installed on Jenkins agent

4. **Docker**
   - Docker installed on Jenkins agent
   - Docker daemon running

5. **Required Tools**
   - AWS CLI
   - kubectl
   - Docker
   - Maven (for Java projects)
   - Gradle (for Java projects using Gradle)
   - Node.js/npm (for Node.js projects)
   - Go (for Golang projects)

## Customization

### Custom Dockerfiles

You can use a Dockerfile from your application repository by setting the `dockerfile` parameter. The path is relative to your repository root:

```groovy
buildAndDeploy([
    language: 'python',
    appName: 'my-app',
    dockerfile: 'docker/Dockerfile.prod',  // Custom Dockerfile path in your repo
    // ... other config
])
```

**Examples of custom Dockerfile paths:**
- `dockerfile: 'Dockerfile.production'` - Dockerfile in root with different name
- `dockerfile: 'docker/Dockerfile'` - Dockerfile in docker subdirectory
- `dockerfile: 'build/Dockerfile.prod'` - Dockerfile in build directory
- `dockerfile: 'deploy/Dockerfile'` - Dockerfile in deploy directory

**Note:** 
- If `dockerfile` is not specified, it defaults to `Dockerfile` in the repository root
- The Dockerfile must exist in your repository (checked during build)
- The build context is always the repository root (`.`), so paths in your Dockerfile should be relative to the repo root

### Kubernetes Manifests

The pipeline will look for Kubernetes manifests in `k8s/` directory:
- `k8s/deployment.yaml` - Deployment manifest
- `k8s/service.yaml` - Service manifest
- `k8s/ingress.yaml` - Ingress manifest

If these files exist, they will be used. Otherwise, a basic deployment will be created automatically.

In your manifests, use `IMAGE_PLACEHOLDER` which will be replaced with the actual image during deployment.

## Pipeline Stages

1. **Validate Configuration** - Validates required parameters and language support
2. **Checkout** - Checks out source code
3. **Build Docker Image** - Builds Docker image based on language
4. **Push to ECR** - Pushes image to AWS ECR
5. **Deploy to EKS** - Deploys application to EKS cluster

## Notes

- Image tags are automatically generated as: `BUILD_NUMBER-GIT_COMMIT_SHORT`
- Images are also tagged as `latest`
- The pipeline automatically handles ECR login
- Deployment waits for rollout completion (5 minute timeout)

