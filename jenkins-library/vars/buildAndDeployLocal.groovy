#!/usr/bin/env groovy

/**
 * Main pipeline entry point for building and deploying applications locally
 * Supports: Python, Java, Node.js, Golang
 * Deployment target: Local Docker
 */
def call(Map config) {
    pipeline {
        agent any

        environment {
            LOCAL_REGISTRY = "${config.localRegistry ?: 'localhost:5000'}"
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
                                buildPythonImage([
                                    language: 'python',
                                    appName: config.appName,
                                    ecrRegistry: env.LOCAL_REGISTRY,
                                    dockerfile: config.dockerfile ?: 'Dockerfile'
                                ])
                                break
                            case 'java':
                                buildJavaImage([
                                    language: 'java',
                                    appName: config.appName,
                                    ecrRegistry: env.LOCAL_REGISTRY,
                                    dockerfile: config.dockerfile ?: 'Dockerfile'
                                ])
                                break
                            case 'node':
                            case 'nodejs':
                                buildNodeImage([
                                    language: 'node',
                                    appName: config.appName,
                                    ecrRegistry: env.LOCAL_REGISTRY,
                                    dockerfile: config.dockerfile ?: 'Dockerfile'
                                ])
                                break
                            case 'golang':
                            case 'go':
                                buildGolangImage([
                                    language: 'golang',
                                    appName: config.appName,
                                    ecrRegistry: env.LOCAL_REGISTRY,
                                    dockerfile: config.dockerfile ?: 'Dockerfile'
                                ])
                                break
                            default:
                                error("Unsupported language: ${config.language}")
                        }
                    }
                }
            }

            stage('Push to Local Registry') {
                steps {
                    script {
                        pushToLocalRegistry([
                            localRegistry: env.LOCAL_REGISTRY,
                            registryUsername: config.registryUsername,
                            registryPassword: config.registryPassword
                        ])
                    }
                }
            }

            stage('Deploy Locally') {
                steps {
                    script {
                        deployToDocker([
                            appName: config.appName,
                            environment: config.environment ?: 'dev',
                            localRegistry: env.LOCAL_REGISTRY,
                            hostPort: config.hostPort ?: 8080,
                            containerPort: config.containerPort ?: 8080,
                            environmentVars: config.environmentVars,
                            volumes: config.volumes,
                            network: config.network,
                            restartPolicy: config.restartPolicy ?: 'unless-stopped'
                        ])
                    }
                }
            }
        }

        post {
            success {
                echo "Local deployment completed successfully"
            }
            failure {
                echo "Local deployment failed"
            }
            always {
                cleanWs()
            }
        }
    }
}