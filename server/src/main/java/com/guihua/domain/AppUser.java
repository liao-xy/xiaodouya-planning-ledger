package com.guihua.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "app_users", uniqueConstraints = @UniqueConstraint(name = "uk_user_username", columnNames = "username"))
public class AppUser {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false, length = 20) private String username;
  @Column(nullable = false, length = 100) private String passwordHash;
  @Column(nullable = false, length = 30) private String nickname;
  @Column(name = "wechat_open_id", unique = true, length = 64) private String wechatOpenId;
  private Instant termsAgreedAt;
  @Column(length = 20) private String termsVersion;
  @Column(nullable = false) private Instant createdAt = Instant.now();
  public Long getId() { return id; }
  public String getUsername() { return username; } public void setUsername(String username) { this.username = username; }
  public String getPasswordHash() { return passwordHash; } public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public String getNickname() { return nickname; } public void setNickname(String nickname) { this.nickname = nickname; }
  public String getWechatOpenId() { return wechatOpenId; } public void setWechatOpenId(String wechatOpenId) { this.wechatOpenId = wechatOpenId; }
  public Instant getTermsAgreedAt() { return termsAgreedAt; } public void setTermsAgreedAt(Instant termsAgreedAt) { this.termsAgreedAt = termsAgreedAt; }
  public String getTermsVersion() { return termsVersion; } public void setTermsVersion(String termsVersion) { this.termsVersion = termsVersion; }
}
