package com.kodehaus.bknd.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

/**
 * Configuration class to load .env file if environment variables are not set.
 * This allows the application to work in both production (with env vars) and development (with .env file).
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class EnvConfig implements ApplicationListener<ContextRefreshedEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(EnvConfig.class);
    private static final String ENV_FILE = ".env";
    private static boolean envLoaded = false;
    
    /**
     * Static method to load .env file before Spring Boot starts.
     * Called from main method to ensure env vars are available early.
     */
    public static void loadEnvFileIfNeeded() {
        if (!envLoaded) {
            new EnvConfig().loadEnvFile();
            envLoaded = true;
        }
    }
    
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (!envLoaded) {
            loadEnvFile();
            envLoaded = true;
        }
    }
    
    /**
     * Loads environment variables from .env file if they are not already set.
     * Production mode: Environment variables are already set, skip .env file.
     * Development mode: Environment variables not set, load from .env file.
     */
    private void loadEnvFile() {
        // Check if we're in production mode (key environment variables are already set)
        String datasourceUrl = System.getenv("SPRING_DATASOURCE_URL");
        if (datasourceUrl != null && !datasourceUrl.isEmpty()) {
            log.info("🔧 Production mode detected: Environment variables are set, skipping .env file");
            return;
        }
        
        // Development mode: Load from .env file
        File envFile = new File(ENV_FILE);
        if (!envFile.exists()) {
            log.warn("⚠️  .env file not found at: {}. Using default values from application.properties", envFile.getAbsolutePath());
            return;
        }
        
        log.info("🔧 Development mode: Loading environment variables from .env file");
        
        try (Scanner scanner = new Scanner(envFile)) {
            int loadedCount = 0;
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                
                // Skip empty lines and comments
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                // Parse KEY=VALUE format
                int equalsIndex = line.indexOf('=');
                if (equalsIndex > 0) {
                    String key = line.substring(0, equalsIndex).trim();
                    String value = line.substring(equalsIndex + 1).trim();
                    
                    // Remove quotes if present
                    if (value.startsWith("\"") && value.endsWith("\"")) {
                        value = value.substring(1, value.length() - 1);
                    } else if (value.startsWith("'") && value.endsWith("'")) {
                        value = value.substring(1, value.length() - 1);
                    }
                    
                    // Only set if not already in environment
                    if (System.getenv(key) == null) {
                        System.setProperty(key, value);
                        loadedCount++;
                    }
                }
            }
            log.info("✅ Loaded {} environment variables from .env file", loadedCount);
        } catch (FileNotFoundException e) {
            log.error("❌ Error reading .env file: {}", e.getMessage());
        } catch (Exception e) {
            log.error("❌ Error processing .env file: {}", e.getMessage(), e);
        }
    }
}

