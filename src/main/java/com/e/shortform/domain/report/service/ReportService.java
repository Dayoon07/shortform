package com.e.shortform.domain.report.service;

import com.e.shortform.domain.report.entity.ReportEntity;
import com.e.shortform.domain.report.enums.TargetType;
import com.e.shortform.domain.report.mapper.ReportMapper;
import com.e.shortform.domain.report.repository.ReportRepo;
import com.e.shortform.domain.report.req.ReportReqDto;
import com.e.shortform.domain.report.vo.ReportVo;
import com.e.shortform.domain.user.entity.UserEntity;
import com.e.shortform.domain.user.repository.UserRepo;
import com.e.shortform.domain.user.req.AuthUserReqDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportService {

    private final UserRepo userRepo;

    private final ReportRepo reportRepo;
    private final ReportMapper reportMapper;

    public void a() {
        System.out.println(TargetType.COMMENT_REPLY);
    }

    public List<ReportVo> getReportAll() {
        return reportMapper.getReportAll();
    }

    public List<ReportEntity> getReportAllJpaVer() {
        return reportRepo.findAll();
    }

    public void saveReport(ReportReqDto reqDto, AuthUserReqDto user) {
        UserEntity reporterUser = userRepo.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다"));
        UserEntity reportedUser = userRepo.findById(reqDto.getReportedUser())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다"));

        ReportEntity a = ReportEntity.builder()
                .targetType(reqDto.getTargetType())
                .targetId(reqDto.getTargetId())
                .reporterUser(reporterUser)
                .reportedUser(reportedUser)
                .reportType(reqDto.getReportType())
                .reportReason(reqDto.getReportReason())
                .build();
        reportRepo.save(a);
    }















}
