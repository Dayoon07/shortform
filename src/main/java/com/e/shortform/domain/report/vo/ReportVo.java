package com.e.shortform.domain.report.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportVo {

    private Long id;
    private String targetType;
    private Long targetId;
    private Long reporterUserId;
    private Long reportedUserId;
    private String reportType;
    private String status;
    private String reviewComment;
    private String reviewedAt;
    private String actionTaken;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}

