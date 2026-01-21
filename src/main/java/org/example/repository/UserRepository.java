package org.example.repository;

import io.ebean.Database;
import org.example.config.DbConfig;
import org.example.entity.User;

public class UserRepository {

    private final Database db = DbConfig.getDatabase();

    public User findByEmail(String email) {
        return db.find(User.class)
                .where()
                .eq("email", email)
                .eq("isDeleted", false)
                .findOne();
    }

    public void save(User user) {
        db.save(user);
    }
}
