package com.e.shortform.domain.report.service;

import com.e.shortform.domain.report.entity.ReportEntity;
import com.e.shortform.domain.report.enums.TargetType;
import com.e.shortform.domain.report.mapper.ReportMapper;
import com.e.shortform.domain.report.repository.ReportRepo;
import com.e.shortform.domain.report.vo.ReportVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class ReportService {

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

}
