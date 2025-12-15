package com.e.shortform.domain.report.repository;

import com.e.shortform.domain.report.entity.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepo extends JpaRepository<ReportEntity, Long> {
}
