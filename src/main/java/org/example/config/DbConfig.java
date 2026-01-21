package org.example.config;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;

import java.io.InputStream;
import java.util.Properties;

public class DbConfig {

    private static Database database;

    public static Database getDatabase() {
        if (database == null) {
            database = buildDatabase();
        }
        return database;
    }

    private static Database buildDatabase() {
        try {
            Properties props = new Properties();

            // ✅ Load all settings from application.properties
            InputStream is = DbConfig.class.getClassLoader().getResourceAsStream("application.properties");
            if (is == null) {
                throw new RuntimeException("application.properties not found in src/main/resources");
            }
            props.load(is);

            // ✅ Create DatabaseConfig and load everything
            DatabaseConfig config = new DatabaseConfig();
            config.loadFromProperties(props);

            config.setName("db");
            config.setDefaultServer(true);
            config.setRegister(true);

            return DatabaseFactory.create(config);

        } catch (Exception e) {
            throw new RuntimeException("Failed to init DB: " + e.getMessage(), e);
        }
    }
}
