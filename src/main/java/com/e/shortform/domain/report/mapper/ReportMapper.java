package com.e.shortform.domain.report.mapper;

import com.e.shortform.domain.report.vo.ReportVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface ReportMapper {

    List<ReportVo> getReportAll();

}
