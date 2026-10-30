package com.guihua.repository;

import com.guihua.domain.MoneyPersonality;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MoneyPersonalityRepository extends JpaRepository<MoneyPersonality, Long> {
  Optional<MoneyPersonality> findByUserId(Long userId);
  void deleteByUserId(Long userId);
}
