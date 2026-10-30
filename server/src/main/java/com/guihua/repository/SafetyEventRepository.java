package com.guihua.repository;
import com.guihua.domain.SafetyEvent;
import org.springframework.data.jpa.repository.JpaRepository;
public interface SafetyEventRepository extends JpaRepository<SafetyEvent, Long> { void deleteByUserId(Long userId); }
