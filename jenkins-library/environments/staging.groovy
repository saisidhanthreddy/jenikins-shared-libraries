/**
 * Staging environment configuration
 */
return [
    namespace: 'staging',
    clusterName: 'staging-eks-cluster',
    replicas: 2,
    port: 8080,
    memoryRequest: '256Mi',
    cpuRequest: '100m',
    memoryLimit: '512Mi',
    cpuLimit: '500m',
    awsRegion: 'us-east-1',
    ecrRegistry: '123456789012.dkr.ecr.us-east-1.amazonaws.com'
]

