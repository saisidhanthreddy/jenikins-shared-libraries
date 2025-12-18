# Jenkins Shared Library Setup Guide

## Quick Start

### 1. Configure Jenkins Shared Library

1. Go to **Jenkins Dashboard** → **Manage Jenkins** → **Configure System**
2. Scroll down to **Global Pipeline Libraries**
3. Click **Add** and configure:
   - **Name**: `jenkins-shared-library` (or your preferred name)
   - **Default version**: `main` (or your branch name)
   - **Retrieval method**: 
     - **Modern SCM** (if using Git)
     - Select your SCM (Git, SVN, etc.)
     - Configure repository URL and credentials

### 2. Repository Structure

If using Git, push this library to a repository. The structure should be:

```
your-repo/
└── jenkins-library/
    ├── vars/
    ├── src/
    ├── resources/
    └── environments/
```

### 3. Configure AWS Credentials

#### Option A: AWS Credentials Plugin
1. Install **AWS Credentials Plugin** in Jenkins
2. Go to **Manage Jenkins** → **Credentials** → **System** → **Global credentials**
3. Add AWS credentials with:
   - **Kind**: AWS Credentials
   - **Access Key ID**: Your AWS access key
   - **Secret Access Key**: Your AWS secret key

#### Option B: IAM Role (Recommended for EC2/EKS)
- Attach IAM role to Jenkins agent with required permissions:
  - `ecr:GetAuthorizationToken`
  - `ecr:BatchCheckLayerAvailability`
  - `ecr:GetDownloadUrlForLayer`
  - `ecr:BatchGetImage`
  - `ecr:PutImage`
  - `eks:DescribeCluster`
  - `eks:ListClusters`

### 4. Configure Kubernetes Credentials

1. Go to **Manage Jenkins** → **Credentials** → **System** → **Global credentials**
2. Add **Secret file** credential:
   - **ID**: `kubeconfig` (or use custom ID in pipeline config)
   - **File**: Upload your kubeconfig file

### 5. Update Environment Configuration

Edit the environment files in `jenkins-library/environments/`:

- `dev.groovy`
- `qa.groovy`
- `staging.groovy`
- `prod.groovy`

Update the following values:
- `ecrRegistry`: Your AWS ECR registry URL (e.g., `123456789012.dkr.ecr.us-east-1.amazonaws.com`)
- `clusterName`: Your EKS cluster names
- `awsRegion`: Your AWS region
- Resource limits and requests as needed

### 6. Create ECR Repositories

Create ECR repositories for your applications:

```bash
aws ecr create-repository --repository-name my-app --region us-east-1
```

### 7. Use in Your Jenkinsfile

Create a `Jenkinsfile` in your application repository:

```groovy
@Library('jenkins-shared-library') _

buildAndDeploy([
    language: 'python',
    appName: 'my-app',
    environment: 'dev',
    ecrRegistry: '123456789012.dkr.ecr.us-east-1.amazonaws.com',
    clusterName: 'dev-eks-cluster',
    awsRegion: 'us-east-1'
])
```

## Required Tools on Jenkins Agent

Ensure the following tools are installed on your Jenkins agent:

- **Docker** - For building images
- **AWS CLI** - For ECR and EKS operations
- **kubectl** - For Kubernetes deployments
- **Maven** (for Java projects)
- **Gradle** (for Java projects using Gradle)
- **Node.js/npm** (for Node.js projects)
- **Go** (for Golang projects)

## Testing

### Test the Library Locally

You can test individual steps using Jenkins Script Console or by creating a test pipeline.

### Test Pipeline Example

```groovy
@Library('jenkins-shared-library') _

pipeline {
    agent any
    stages {
        stage('Test') {
            steps {
                script {
                    def config = [
                        language: 'python',
                        appName: 'test-app',
                        environment: 'dev',
                        ecrRegistry: '123456789012.dkr.ecr.us-east-1.amazonaws.com',
                        clusterName: 'dev-eks-cluster'
                    ]
                    validateConfig(config)
                }
            }
        }
    }
}
```

## Troubleshooting

### Common Issues

1. **Library not found**
   - Verify library name matches exactly
   - Check library is configured in Jenkins
   - Ensure repository is accessible

2. **ECR login fails**
   - Verify AWS credentials are configured
   - Check IAM permissions
   - Verify ECR repository exists

3. **EKS deployment fails**
   - Verify kubeconfig is correct
   - Check kubectl can access cluster
   - Verify namespace exists or has permissions to create

4. **Environment config not found**
   - Ensure environment file exists in `environments/` directory
   - Check file naming matches environment parameter

## Next Steps

- Customize Dockerfiles in `resources/` directory
- Add more environment configurations
- Extend pipeline with additional stages (testing, security scanning, etc.)
- Add notification steps (Slack, email, etc.)

