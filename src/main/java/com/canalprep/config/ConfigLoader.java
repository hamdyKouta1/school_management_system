package com.canalprep.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ConfigLoader utility class for reading application.properties with environment override support.
 * 
 * This class loads configuration from application.properties and supports environment-specific
 * overrides using suffixes like .dev, .staging, .prod based on the app.env property.
 * 
 * Usage:
 * - ConfigLoader.getString("db.url") - gets base value
 * - If app.env=dev, it will check for db.url.dev first, then fallback to db.url
 * - ConfigLoader.getInt("server.port") - gets integer value with same override logic
 * - ConfigLoader.getBoolean("cors.allow_credentials") - gets boolean value
 */
public class ConfigLoader {
    private static final Logger logger = Logger.getLogger(ConfigLoader.class.getName());
    private static final Properties properties = new Properties();
    private static String environment = null;
    private static boolean initialized = false;
    
    // Static initializer to load properties on class loading
    static {
        initialize();
    }
    
    /**
     * Initialize the configuration loader by reading application.properties
     */
    private static void initialize() {
        if (initialized) {
            return;
        }
        
        try (InputStream input = ConfigLoader.class.getClassLoader().getResourceAsStream("application.properties")) {
            if (input == null) {
                logger.log(Level.WARNING, "application.properties file not found in classpath. Using default values.");
                initialized = true;
                return;
            }
            
            properties.load(input);
            
            // Load external secure configuration if available
            loadExternalSecureConfig();
            
            // Detect environment from app.env property or system property
            environment = System.getProperty("app.env");
            if (environment == null) {
                environment = properties.getProperty("app.env");
            }
            
            logger.log(Level.INFO, "Configuration loaded successfully. Environment: " + 
                      (environment != null ? environment : "default"));
            
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to load application.properties", e);
        }
        
        initialized = true;
    }
    
    /**
     * Load external secure configuration files
     * Checks for secure.properties in multiple locations:
     * 1. System property: config.secure.path
     * 2. Environment variable: CONFIG_SECURE_PATH  
     * 3. ./config/secure.properties (relative to working directory)
     * 4. /etc/app/secure.properties (Linux/Unix systems)
     */
    private static void loadExternalSecureConfig() {
        String[] possiblePaths = {
            System.getProperty("config.secure.path"),
            System.getenv("CONFIG_SECURE_PATH"),
            "./config/secure.properties",
            "/etc/app/secure.properties",
            "config/secure.properties"
        };
        
        for (String path : possiblePaths) {
            if (path != null && Files.exists(Paths.get(path))) {
                try (FileInputStream fis = new FileInputStream(path)) {
                    Properties secureProps = new Properties();
                    secureProps.load(fis);
                    
                    // Merge secure properties (they override existing ones)
                    for (String key : secureProps.stringPropertyNames()) {
                        properties.setProperty(key, secureProps.getProperty(key));
                    }
                    
                    logger.log(Level.INFO, "Loaded secure configuration from: " + path);
                    return; // Load only the first found file
                    
                } catch (IOException e) {
                    logger.log(Level.WARNING, "Failed to load secure config from: " + path, e);
                }
            }
        }
        
        logger.log(Level.INFO, "No external secure configuration file found. Using internal configuration only.");
    }
    
    /**
     * Get a string property value with environment override support
     * @param key the property key
     * @return the property value, or null if not found
     */
    public static String getString(String key) {
        return getString(key, null);
    }
    
    /**
     * Get a string property value with environment override support and default value
     * @param key the property key
     * @param defaultValue the default value if property is not found
     * @return the property value, or defaultValue if not found
     */
    public static String getString(String key, String defaultValue) {
        // First try environment-specific key if environment is set
        if (environment != null && !environment.trim().isEmpty()) {
            String envKey = key + "." + environment;
            String envValue = properties.getProperty(envKey);
            if (envValue != null && !envValue.trim().isEmpty()) {
                return envValue.trim();
            }
        }
        
        // Fallback to base key
        String value = properties.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        
        return defaultValue;
    }
    
    /**
     * Get an integer property value with environment override support
     * @param key the property key
     * @param defaultValue the default value if property is not found or invalid
     * @return the property value as integer, or defaultValue if not found/invalid
     */
    public static int getInt(String key, int defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid integer value for key '" + key + "': " + value + 
                      ". Using default: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get a long property value with environment override support
     * @param key the property key
     * @param defaultValue the default value if property is not found or invalid
     * @return the property value as long, or defaultValue if not found/invalid
     */
    public static long getLong(String key, long defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            logger.log(Level.WARNING, "Invalid long value for key '" + key + "': " + value + 
                      ". Using default: " + defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * Get a boolean property value with environment override support
     * @param key the property key
     * @param defaultValue the default value if property is not found
     * @return the property value as boolean, or defaultValue if not found
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        String value = getString(key);
        if (value == null) {
            return defaultValue;
        }
        
        return "true".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || "1".equals(value);
    }
    
    /**
     * Get the current environment setting
     * @return the environment string (dev, staging, prod) or null if not set
     */
    public static String getEnvironment() {
        return environment;
    }
    
    /**
     * Check if a property exists (considering environment overrides)
     * @param key the property key
     * @return true if the property exists, false otherwise
     */
    public static boolean hasProperty(String key) {
        return getString(key) != null;
    }
    
    /**
     * Reload the configuration (useful for testing or dynamic config changes)
     */
    public static void reload() {
        initialized = false;
        properties.clear();
        environment = null;
        initialize();
    }
    
    /**
     * Get all loaded properties (for debugging)
     * @return a copy of the loaded properties
     */
    public static Properties getAllProperties() {
        Properties copy = new Properties();
        copy.putAll(properties);
        return copy;
    }
}