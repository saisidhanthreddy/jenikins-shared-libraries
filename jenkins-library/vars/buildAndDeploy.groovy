#!/usr/bin/env groovy

/**
 * Main pipeline entry point for building and deploying applications
 * Supports: Python, Java, Node.js, Golang
 * Deployment target: EKS cluster
 */
def call(Map config) {
    pipeline {
        agent any
        
        environment {
            AWS_REGION = "${config.awsRegion ?: 'us-east-1'}"
            ECR_REGISTRY = "${config.ecrRegistry ?: ''}"
        }
        
        stages {
            stage('Validate Configuration') {
                steps {
                    script {
                        validateConfig(config)
                    }
                }
            }
            stage('Checkout') {
                steps {
                    script {
                        checkout scm
                        // Generate image tag
                        def gitCommit = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                        env.IMAGE_TAG = "${env.BUILD_NUMBER}-${gitCommit}"
                    }
                }
            }
            
            stage('Build Docker Image') {
                steps {
                    script {
                        switch(config.language.toLowerCase()) {
                            case 'python':
                                buildPythonImage(config)
                                break
                            case 'java':
                                buildJavaImage(config)
                                break
                            case 'node':
                            case 'nodejs':
                                buildNodeImage(config)
                                break
                            case 'golang':
                            case 'go':
                                buildGolangImage(config)
                                break
                            default:
                                error("Unsupported language: ${config.language}")
                        }
                    }
                }
            }
            
            stage('Push to ECR') {
                steps {
                    script {
                        pushToECR(config)
                    }
                }
            }
            
            stage('Deploy to EKS') {
                steps {
                    script {
                        deployToEKS(config)
                    }
                }
            }
        }
        
        post {
            success {
                echo "Pipeline completed successfully"
            }
            failure {
                echo "Pipeline failed"
            }
            always {
                cleanWs()
            }
        }
    }
}

