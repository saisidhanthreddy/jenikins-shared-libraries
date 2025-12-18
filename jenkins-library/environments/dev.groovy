/**
 * Development environment configuration
 */
return [
    namespace: 'dev',
    clusterName: 'dev-eks-cluster',
    replicas: 1,
    port: 8080,
    memoryRequest: '128Mi',
    cpuRequest: '50m',
    memoryLimit: '256Mi',
    cpuLimit: '200m',
    awsRegion: 'us-east-1',
    ecrRegistry: '123456789012.dkr.ecr.us-east-1.amazonaws.com'
]

