package com.guihua.repository;

import com.guihua.domain.DailyQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

public interface DailyQuoteRepository extends JpaRepository<DailyQuote, LocalDate> {}
