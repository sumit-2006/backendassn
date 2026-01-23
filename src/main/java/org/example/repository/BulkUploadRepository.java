package org.example.repository;

import io.ebean.DB;
import io.ebean.Database;
import org.example.entity.BulkUpload;

public class BulkUploadRepository {
    private final Database db = DB.getDefault();

    public void save(BulkUpload upload) {
        db.save(upload);
    }

    public BulkUpload findById(Long id) {
        return db.find(BulkUpload.class, id);
    }
}