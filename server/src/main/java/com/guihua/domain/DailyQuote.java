package com.guihua.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "daily_quotes")
public class DailyQuote {
  @Id
  @Column(name = "quote_date", nullable = false)
  private LocalDate date;
  @Column(nullable = false, length = 500)
  private String content;
  @Column(length = 100)
  private String source;
  public DailyQuote() {}
  public DailyQuote(LocalDate date, String content, String source) { this.date = date; this.content = content; this.source = source; }
  public LocalDate getDate() { return date; }
  public String getContent() { return content; }
  public String getSource() { return source; }
}
