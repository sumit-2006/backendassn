package org.example.repository;

import io.ebean.Database;
import org.example.config.DbConfig;
import org.example.entity.KycRecord;

public class KycRepository {
    private final Database db = DbConfig.getDatabase();

    public void save(KycRecord record) {
        db.save(record);
    }

    public KycRecord findByUserId(Long userId) {
        return db.find(KycRecord.class)
                .where()
                .eq("user.id", userId)
                .eq("isDeleted", false)
                .findOne();
    }
}