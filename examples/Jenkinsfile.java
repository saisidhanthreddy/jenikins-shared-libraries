@Library('jenkins-shared-library') _

buildAndDeploy([
    language: 'java',
    appName: 'java-service',
    environment: 'staging',
    ecrRegistry: '123456789012.dkr.ecr.us-east-1.amazonaws.com',
    clusterName: 'staging-eks-cluster',
    awsRegion: 'us-east-1',
    dockerfile: 'Dockerfile'  // Optional: defaults to 'Dockerfile', can specify custom path
])

