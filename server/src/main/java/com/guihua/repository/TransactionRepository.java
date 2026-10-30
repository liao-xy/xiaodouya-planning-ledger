package com.guihua.repository;
import com.guihua.domain.TransactionRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
public interface TransactionRepository extends JpaRepository<TransactionRecord, Long> {
  List<TransactionRecord> findByUserIdAndOccurredOnBetweenOrderByOccurredOnDescIdDesc(Long userId, LocalDate from, LocalDate to);
  List<TransactionRecord> findByUserIdOrderByOccurredOnDescIdDesc(Long userId);
  List<TransactionRecord> findTop200ByUserIdOrderByOccurredOnDescIdDesc(Long userId);
  void deleteByUserId(Long userId);
}
