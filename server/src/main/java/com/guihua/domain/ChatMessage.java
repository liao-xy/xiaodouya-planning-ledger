package com.guihua.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false) private Long sessionId;
  @Column(nullable = false) private String role;
  @Column(nullable = false, length = 4000) private String content;
  @Column(length = 1000) private String evidence;
  @Column(nullable = false) private Instant createdAt = Instant.now();
  public Long getId() { return id; } public Long getSessionId() { return sessionId; } public void setSessionId(Long sessionId) { this.sessionId = sessionId; }
  public String getRole() { return role; } public void setRole(String role) { this.role = role; }
  public String getContent() { return content; } public void setContent(String content) { this.content = content; }
  public String getEvidence() { return evidence; } public void setEvidence(String evidence) { this.evidence = evidence; }
  public Instant getCreatedAt() { return createdAt; }
}
