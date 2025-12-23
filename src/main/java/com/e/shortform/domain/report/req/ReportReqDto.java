package com.e.shortform.domain.report.req;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReportReqDto {

    private String targetType;
    private Long targetId;
    private Long reportedUser;
    private String reportType;
    private String reportReason;

}
