/**
 * Production environment configuration
 */
return [
    namespace: 'production',
    clusterName: 'prod-eks-cluster',
    replicas: 3,
    port: 8080,
    memoryRequest: '512Mi',
    cpuRequest: '200m',
    memoryLimit: '1Gi',
    cpuLimit: '1000m',
    awsRegion: 'us-east-1',
    ecrRegistry: '123456789012.dkr.ecr.us-east-1.amazonaws.com'
]

