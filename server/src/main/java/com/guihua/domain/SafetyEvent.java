package com.guihua.domain;

import jakarta.persistence.*;
import java.time.Instant;

/** 仅保存处置所需的最小片段和分类，供运营核查与依法配合处置。 */
@Entity
@Table(name = "safety_events")
public class SafetyEvent {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false) private Long userId;
  @Column(nullable = false, length = 40) private String scene;
  @Column(nullable = false, length = 60) private String riskType;
  @Column(nullable = false, length = 600) private String contentExcerpt;
  @Column(nullable = false) private Instant createdAt = Instant.now();
  public Long getId() { return id; }
  public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
  public String getScene() { return scene; } public void setScene(String scene) { this.scene = scene; }
  public String getRiskType() { return riskType; } public void setRiskType(String riskType) { this.riskType = riskType; }
  public String getContentExcerpt() { return contentExcerpt; } public void setContentExcerpt(String contentExcerpt) { this.contentExcerpt = contentExcerpt; }
  public Instant getCreatedAt() { return createdAt; }
}
