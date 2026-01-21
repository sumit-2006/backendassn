package org.example.entity;

import io.ebean.Model;
import io.ebean.annotation.WhenCreated;
import io.ebean.annotation.WhenModified;
import jakarta.persistence.*;

import java.time.Instant;

@MappedSuperclass
public abstract class BaseEntity extends Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @WhenCreated
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @WhenModified
    @Column(nullable = false)
    private Instant updatedAt;

    @Column(nullable = false)
    private Boolean isDeleted = false;

    // ✅ Soft delete method
    public void softDelete() {
        this.isDeleted = true;
        this.save();
    }

    // Getters/Setters
    public Long getId() { return id; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Boolean getIsDeleted() { return isDeleted; }

    public void setIsDeleted(Boolean isDeleted) { this.isDeleted = isDeleted; }
}
