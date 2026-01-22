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

    // Add to UserRepository.java
    public io.ebean.PagedList<User> findPaged(int page, int size, org.example.entity.enums.Role role) {
        var query = db.find(User.class)
                .where()
                .eq("isDeleted", false);

        if (role != null) {
            query.eq("role", role);
        }

        return query.setFirstRow(page * size)
                .setMaxRows(size)
                .findPagedList();
    }
}
