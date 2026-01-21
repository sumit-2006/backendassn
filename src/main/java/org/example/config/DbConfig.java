package org.example.config;

import io.ebean.Database;
import io.ebean.DatabaseFactory;
import io.ebean.config.DatabaseConfig;

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
        Properties props = new Properties();

        // Load from application.properties manually OR hardcode for now
        props.put("datasource.db.url", System.getProperty("db.url",
                "jdbc:mysql://localhost:3306/role_learning_db?useSSL=false&allowPublicKeyRetrieval=true"));
        props.put("datasource.db.username", System.getProperty("db.username", "root"));
        props.put("datasource.db.password", System.getProperty("db.password", "Sumit@2006"));
        props.put("datasource.db.driver", System.getProperty("db.driver", "com.mysql.cj.jdbc.Driver"));

        DatabaseConfig config = new DatabaseConfig();
        config.loadFromProperties(props);
        config.setName("db");

        // ✅ Scan entities automatically
        config.setDefaultServer(true);
        config.setRegister(true);

        return DatabaseFactory.create(config);
    }
}
