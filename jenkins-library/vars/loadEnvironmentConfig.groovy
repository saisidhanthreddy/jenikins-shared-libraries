#!/usr/bin/env groovy

/**
 * Load environment-specific configuration
 */
def call(String environment) {
    def configFile = "environments/${environment}.groovy"
    
    // Try to load from library resources first
    try {
        def configContent = libraryResource(configFile)
        return evaluate(configContent)
    } catch (Exception e) {
        // Fallback to file system if not in library resources
        if (fileExists("jenkins-library/${configFile}")) {
            def configContent = readFile("jenkins-library/${configFile}")
            return evaluate(configContent)
        } else {
            error("Environment configuration file not found: ${configFile}")
        }
    }
}

