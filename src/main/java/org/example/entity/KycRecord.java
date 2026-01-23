package org.example.entity;

import io.ebean.annotation.DbDefault;
import io.ebean.annotation.DbJson;
import jakarta.persistence.*;
import org.example.entity.enums.GovtIdType;
import org.example.entity.enums.KycStatus;
import java.time.LocalDate;
import java.util.List;

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
    // Add these fields to your existing class

    @Column(name = "ai_status")
    private String aiStatus; // Or create an Enum if preferred

    @Column(name = "ai_confidence_score")
    private Integer aiConfidenceScore;

    @Column(name = "ai_recommendation")
    private String aiRecommendation;

    @DbJson // Requires Ebean JSON support
    @Column(name = "ai_risk_flags", columnDefinition = "json")
    private List<String> aiRiskFlags;

    // Add Getters and Setters for these new fields
    public String getAiStatus() { return aiStatus; }
    public void setAiStatus(String aiStatus) { this.aiStatus = aiStatus; }

    public Integer getAiConfidenceScore() { return aiConfidenceScore; }
    public void setAiConfidenceScore(Integer aiConfidenceScore) { this.aiConfidenceScore = aiConfidenceScore; }

    public String getAiRecommendation() { return aiRecommendation; }
    public void setAiRecommendation(String aiRecommendation) { this.aiRecommendation = aiRecommendation; }

    public List<String> getAiRiskFlags() { return aiRiskFlags; }
    public void setAiRiskFlags(List<String> aiRiskFlags) { this.aiRiskFlags = aiRiskFlags; }

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