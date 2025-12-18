package org.jenkins

/**
 * Utility class for deployment operations
 */
class DeploymentUtils {
    
    /**
     * Validate required configuration parameters
     */
    static void validateConfig(Map config) {
        def required = ['language', 'appName', 'environment', 'ecrRegistry', 'clusterName']
        def missing = required.findAll { !config.containsKey(it) || !config[it] }
        
        if (missing) {
            throw new Exception("Missing required configuration: ${missing.join(', ')}")
        }
    }
    
    /**
     * Get supported languages
     */
    static List<String> getSupportedLanguages() {
        return ['python', 'java', 'node', 'nodejs', 'golang', 'go']
    }
    
    /**
     * Sanitize image name
     */
    static String sanitizeImageName(String name) {
        return name.toLowerCase().replaceAll(/[^a-z0-9-]/, '-')
    }
}

