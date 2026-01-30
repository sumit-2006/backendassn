package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class EnvironmentConfig {

    private static final Properties props = new Properties();

    static {
        loadConfiguration();
    }

    private static void loadConfiguration() {
        // Load from .env file if exists
        try {
            if (Files.exists(Paths.get(".env"))) {
                Properties envProps = new Properties();
                try (InputStream is = Files.newInputStream(Paths.get(".env"))) {
                    envProps.load(is);
                    envProps.forEach((key, value) -> System.setProperty(key.toString(), value.toString()));
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load .env file: " + e.getMessage());
        }

        // Load application.properties
        try (InputStream is = EnvironmentConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (is != null) {
                props.load(is);
            }
        } catch (IOException e) {
            System.err.println("Warning: Could not load application.properties: " + e.getMessage());
        }
    }

    public static String get(String key) {
        // Priority: System env > System properties > .env file > application.properties
        String value = System.getenv(key);
        if (value != null) return value;

        value = System.getProperty(key);
        if (value != null) return value;

        return props.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }

    public static void validateRequiredProperties() {
        String[] required = {"JWT_SECRET", "DB_PASSWORD"};

        for (String prop : required) {
            String value = get(prop);
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalStateException("Required environment variable missing: " + prop);
            }
        }

        // Validate JWT secret strength
        String jwtSecret = get("JWT_SECRET");
        if (jwtSecret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must be at least 32 characters long");
        }
    }
}
