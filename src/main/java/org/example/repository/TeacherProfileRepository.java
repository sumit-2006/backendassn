package org.example.repository;

import io.ebean.Database;
import org.example.config.DbConfig;
import org.example.entity.TeacherProfile;

public class TeacherProfileRepository {
    private final Database db = DbConfig.getDatabase();

    public void save(TeacherProfile profile) {
        db.save(profile);
    }
}