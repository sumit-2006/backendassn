package org.example.entity;

import io.ebean.annotation.DbJson;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.example.entity.enums.BulkUploadStatus;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "bulk_uploads")
public class BulkUpload extends BaseEntity {

    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    @Column(nullable = false)
    private String uploadType;

    private Integer totalRecords = 0;
    private Integer successCount = 0;
    private Integer failureCount = 0;

    @Enumerated(EnumType.STRING)
    private BulkUploadStatus status = BulkUploadStatus.IN_PROGRESS;

    @DbJson // Requires Ebean JSON support
    @Column(columnDefinition = "json")
    private List<String> errorReport;


}