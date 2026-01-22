package org.example.repository;

import io.ebean.Database;
import io.ebean.PagedList;
import org.example.config.DbConfig;
import org.example.entity.KycRecord;
import org.example.entity.enums.KycStatus;

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
    public PagedList<KycRecord> findPending(int page, int size) {
        return db.find(KycRecord.class)
                .fetch("user") // Critical: Load user details (Name, Email)
                .where()
                .eq("status", KycStatus.SUBMITTED) // Only fetch Submitted, not Approved/Rejected
                .eq("isDeleted", false)
                .setFirstRow(page * size)
                .setMaxRows(size)
                .findPagedList();
    }
    public KycRecord findById(Long id) {
        return db.find(KycRecord.class, id);
    }
}