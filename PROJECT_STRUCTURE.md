# Jenkins Shared Library Project Structure

## Complete Directory Tree

```
jenkins_project_sample/
├── .gitignore
├── SETUP.md                          # Setup instructions
├── PROJECT_STRUCTURE.md              # This file
│
├── jenkins-library/                  # Main shared library directory
│   ├── README.md                     # Library documentation
│   │
│   ├── vars/                        # Pipeline step definitions (global variables)
│   │   ├── buildAndDeploy.groovy    # Main pipeline entry point
│   │   ├── buildPythonImage.groovy  # Python image builder
│   │   ├── buildJavaImage.groovy    # Java image builder
│   │   ├── buildNodeImage.groovy    # Node.js image builder
│   │   ├── buildGolangImage.groovy  # Golang image builder
│   │   ├── pushToECR.groovy         # ECR push step
│   │   ├── deployToEKS.groovy       # EKS deployment step
│   │   ├── loadEnvironmentConfig.groovy  # Environment config loader
│   │   └── validateConfig.groovy    # Configuration validator
│   │
│   ├── src/                          # Groovy source classes
│   │   └── org/
│   │       └── jenkins/
│   │           └── DeploymentUtils.groovy  # Utility class
│   │
│   ├── resources/                    # Resource files (Dockerfile templates)
│   │   ├── Dockerfile.python
│   │   ├── Dockerfile.java
│   │   ├── Dockerfile.node
│   │   └── Dockerfile.golang
│   │
│   └── environments/                 # Environment configurations
│       ├── dev.groovy                # Development environment
│       ├── qa.groovy                 # QA environment
│       ├── staging.groovy            # Staging environment
│       └── prod.groovy               # Production environment
│
└── examples/                         # Example Jenkinsfiles
    ├── Jenkinsfile.python
    ├── Jenkinsfile.java
    ├── Jenkinsfile.node
    └── Jenkinsfile.golang
```

## Key Components

### 1. Main Pipeline (`vars/buildAndDeploy.groovy`)
- Entry point for all build and deploy operations
- Orchestrates the entire CI/CD pipeline
- Supports multiple languages and environments

### 2. Language-Specific Builders (`vars/build*.groovy`)
- `buildPythonImage.groovy` - Builds Python Docker images
- `buildJavaImage.groovy` - Builds Java Docker images (Maven/Gradle)
- `buildNodeImage.groovy` - Builds Node.js Docker images
- `buildGolangImage.groovy` - Builds Golang Docker images

### 3. Deployment Steps
- `pushToECR.groovy` - Pushes images to AWS ECR
- `deployToEKS.groovy` - Deploys to EKS cluster

### 4. Configuration Management
- `loadEnvironmentConfig.groovy` - Loads environment-specific configs
- `validateConfig.groovy` - Validates pipeline configuration
- `environments/*.groovy` - Environment-specific settings

### 5. Resources
- Dockerfile templates for each language
- Can be used as reference or copied to projects

## Usage Flow

1. **Developer creates Jenkinsfile** in their project
2. **Jenkinsfile calls** `buildAndDeploy()` with configuration
3. **Pipeline validates** configuration
4. **Pipeline builds** Docker image based on language
5. **Pipeline pushes** image to ECR
6. **Pipeline deploys** to EKS using environment config

## Configuration Parameters

### Required Parameters
- `language` - Application language (python, java, node, golang)
- `appName` - Application name
- `environment` - Target environment (dev, qa, staging, prod)
- `ecrRegistry` - AWS ECR registry URL
- `clusterName` - EKS cluster name

### Optional Parameters
- `awsRegion` - AWS region (default: us-east-1)
- `imageName` - Custom image name (default: appName)
- `dockerfile` - Custom Dockerfile path (default: Dockerfile)
- `namespace` - Kubernetes namespace (default: from env config)
- `kubeconfigCredentialId` - Jenkins credential ID for kubeconfig (default: kubeconfig)

## Environment Configuration

Each environment file (`environments/*.groovy`) defines:
- Kubernetes namespace
- EKS cluster name
- Replica count
- Resource requests/limits
- Container port
- AWS region and ECR registry

## Next Steps

1. Review and customize environment configurations
2. Update ECR registry URLs and cluster names
3. Configure Jenkins shared library
4. Test with a sample application
5. Customize Dockerfiles as needed

