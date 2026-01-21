package org.example.repository;

import io.ebean.Database;
import org.example.config.DbConfig;
import org.example.entity.InvalidatedToken;

import java.time.Instant;

public class TokenRepository {

    private final Database db = DbConfig.getDatabase();

    public void invalidate(String tokenHash, Instant exp) {
        InvalidatedToken t = new InvalidatedToken();
        t.setTokenHash(tokenHash);
        t.setExpiresAt(exp);
        db.save(t);
    }

    public boolean isInvalidated(String tokenHash) {
        return db.find(InvalidatedToken.class)
                .where()
                .eq("tokenHash", tokenHash)
                .findCount() > 0;
    }
}
