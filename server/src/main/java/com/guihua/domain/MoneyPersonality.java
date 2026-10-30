package com.guihua.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "money_personalities", uniqueConstraints = @UniqueConstraint(name = "uk_personality_user", columnNames = "user_id"))
public class MoneyPersonality {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(name = "user_id", nullable = false) private Long userId;
  @Column(nullable = false, length = 50) private String answers;
  @Column(nullable = false, length = 20) private String resultCode;
  @Column(nullable = false, length = 50) private String resultTitle;
  @Column(length = 500) private String imageUrl;
  @Column(nullable = false) private int planningScore;
  @Column(nullable = false) private int experienceScore;
  @Column(nullable = false) private int recordScore;
  @Column(nullable = false) private Instant updatedAt = Instant.now();
  public Long getId() { return id; }
  public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
  public String getAnswers() { return answers; } public void setAnswers(String answers) { this.answers = answers; }
  public String getResultCode() { return resultCode; } public void setResultCode(String resultCode) { this.resultCode = resultCode; }
  public String getResultTitle() { return resultTitle; } public void setResultTitle(String resultTitle) { this.resultTitle = resultTitle; }
  public String getImageUrl() { return imageUrl; } public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
  public int getPlanningScore() { return planningScore; } public void setPlanningScore(int planningScore) { this.planningScore = planningScore; }
  public int getExperienceScore() { return experienceScore; } public void setExperienceScore(int experienceScore) { this.experienceScore = experienceScore; }
  public int getRecordScore() { return recordScore; } public void setRecordScore(int recordScore) { this.recordScore = recordScore; }
  public Instant getUpdatedAt() { return updatedAt; } public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
