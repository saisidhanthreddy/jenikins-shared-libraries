#!/usr/bin/env groovy

/**
 * Validate required configuration parameters
 */
def call(Map config) {
    def required = ['language', 'appName', 'environment']

    // For AWS deployment (buildAndDeploy), require ECR and cluster
    // For local deployment (buildAndDeployLocal), these are optional
    if (!config.containsKey('localRegistry')) {
        required.addAll(['ecrRegistry', 'clusterName'])
    }

    def missing = required.findAll { !config.containsKey(it) || !config[it] }

    if (missing) {
        error("Missing required configuration parameters: ${missing.join(', ')}")
    }

    def supportedLanguages = ['python', 'java', 'node', 'nodejs', 'golang', 'go']
    if (!supportedLanguages.contains(config.language.toLowerCase())) {
        error("Unsupported language: ${config.language}. Supported: ${supportedLanguages.join(', ')}")
    }
}

