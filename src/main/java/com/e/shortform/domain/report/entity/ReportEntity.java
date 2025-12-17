package com.e.shortform.domain.report.entity;

import com.e.shortform.domain.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHORTFORM_REPORTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportEntity {

    @Id
    @Column(name = "ID", nullable = false, unique = true)
    private Long id;

    @Column(name = "TARGET_TYPE", nullable = false)
    private String targetType;

    @Column(name = "TARGET_ID", nullable = false)
    private Long targetId;

    @ManyToOne
    @JoinColumn(name = "REPORTER_USER_ID", nullable = false,
            foreignKey = @ForeignKey(name = "FK_REPORT_REPORTER"))
    private UserEntity reporterUser;

    @ManyToOne
    @JoinColumn(name = "REPORTED_USER_ID", nullable = false,
            foreignKey = @ForeignKey(name = "FK_REPORT_REPORTED"))
    private UserEntity reportedUser;

    @Column(name = "REPORT_TYPE", nullable = false)
    private String reportType;

    @Column(name = "REPORT_REASON")
    private String reportReason;

    @Column(name = "STATUS", nullable = false)
    private String status;

    @Column(name = "ACTION_TAKEN")
    private String actionTaken;

    @Lob
    @Column(name = "REVIEW_COMMENT")
    private String reviewComment;

    @Column(name = "REVIEWED_AT")
    private String reviewedAt;

    @CreationTimestamp
    @Column(name = "CREATED_AT", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;

}
