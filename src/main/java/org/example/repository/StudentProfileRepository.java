package org.example.repository;

import io.ebean.Database;
import org.example.config.DbConfig;
import org.example.entity.StudentProfile;

public class StudentProfileRepository {
    private final Database db = DbConfig.getDatabase();

    public void save(StudentProfile profile) {
        db.save(profile);
    }
}