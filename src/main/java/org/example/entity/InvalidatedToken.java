package org.example.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name="invalidated_tokens")
public class InvalidatedToken extends BaseEntity {

    @Column(nullable = false, unique = true, length = 256)
    private String tokenHash;

    @Column(nullable = false)
    private Instant expiresAt;

    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
