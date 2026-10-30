package com.guihua.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "auth_tokens")
public class AuthToken {
  @Id @Column(length = 64) private String token;
  @Column(nullable = false) private Long userId;
  @Column(nullable = false) private Instant expiresAt;
  public String getToken() { return token; } public void setToken(String token) { this.token = token; }
  public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
  public Instant getExpiresAt() { return expiresAt; } public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
