package org.example.repository;

import io.ebean.Database;
import org.example.config.DbConfig;
import org.example.entity.StudentProfile;

public class StudentProfileRepository {
    private final Database db = DbConfig.getDatabase();

    public void save(StudentProfile profile) {
        db.save(profile);
    }

    // Add this method inside the class
    public org.example.entity.StudentProfile findByUserId(Long userId) {
        return org.example.config.DbConfig.getDatabase()
                .find(org.example.entity.StudentProfile.class)
                .where().eq("user.id", userId)
                .findOne();
    }

}