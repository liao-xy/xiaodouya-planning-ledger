package com.guihua.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "safety_reports")
public class SafetyReport {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false) private Long userId;
  @Column(nullable = false, length = 1000) private String content;
  @Column(nullable = false, length = 20) private String status = "PENDING";
  @Column(nullable = false) private Instant createdAt = Instant.now();
  public Long getId() { return id; }
  public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
  public String getContent() { return content; } public void setContent(String content) { this.content = content; }
  public String getStatus() { return status; } public void setStatus(String status) { this.status = status; }
  public Instant getCreatedAt() { return createdAt; }
}
