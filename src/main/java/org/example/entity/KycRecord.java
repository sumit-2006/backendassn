package org.example.entity;

import io.ebean.annotation.DbDefault;
import jakarta.persistence.*;
import org.example.entity.enums.GovtIdType;
import org.example.entity.enums.KycStatus;
import java.time.LocalDate;

@Entity
@Table(name = "kyc_records")
public class KycRecord extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GovtIdType govtIdType;

    @Column(nullable = false)
    private String govtIdNumber;

    private String documentPath;

    @Enumerated(EnumType.STRING)
    @DbDefault("PENDING")
    private KycStatus status = KycStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminRemarks;

    // Getters and Setters
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dob) { this.dateOfBirth = dob; }
    public GovtIdType getGovtIdType() { return govtIdType; }
    public void setGovtIdType(GovtIdType type) { this.govtIdType = type; }
    public String getGovtIdNumber() { return govtIdNumber; }
    public void setGovtIdNumber(String num) { this.govtIdNumber = num; }
    public String getDocumentPath() { return documentPath; }
    public void setDocumentPath(String path) { this.documentPath = path; }
    public KycStatus getStatus() { return status; }
    public void setStatus(KycStatus status) { this.status = status; }
    public String getAdminRemarks() { return adminRemarks; }
    public void setAdminRemarks(String remarks) { this.adminRemarks = remarks; }
}