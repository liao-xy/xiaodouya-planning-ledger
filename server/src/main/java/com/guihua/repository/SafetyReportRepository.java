package com.guihua.repository;
import com.guihua.domain.SafetyReport;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SafetyReportRepository extends JpaRepository<SafetyReport, Long> { void deleteByUserId(Long userId); }
