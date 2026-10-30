package com.guihua.repository;
import com.guihua.domain.MoneyPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface MoneyPlanRepository extends JpaRepository<MoneyPlan, Long> { List<MoneyPlan> findByUserIdOrderByIdDesc(Long userId); void deleteByUserId(Long userId); }
