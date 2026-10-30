package com.guihua.service;

import com.guihua.api.ApiDtos.*;
import com.guihua.domain.*;
import com.guihua.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.UUID;

@Service
public class AuthService {
  private final AppUserRepository users; private final AuthTokenRepository tokens; private final WechatIdentityClient wechat;
  private final TransactionRepository transactions; private final MoneyPlanRepository plans; private final MoneyPersonalityRepository personalities;
  private final ChatSessionRepository sessions; private final ChatMessageRepository messages;
  public AuthService(AppUserRepository users, AuthTokenRepository tokens, WechatIdentityClient wechat, TransactionRepository transactions,
                     MoneyPlanRepository plans, MoneyPersonalityRepository personalities, ChatSessionRepository sessions,
                     ChatMessageRepository messages) {
    this.users = users; this.tokens = tokens; this.wechat = wechat; this.transactions = transactions; this.plans = plans;
    this.personalities = personalities; this.sessions = sessions; this.messages = messages;
  }
  public AuthView wechatLogin(WechatLoginRequest request) {
    String openId = wechat.openIdFor(request.code());
    AppUser user = users.findByWechatOpenId(openId).orElseGet(() -> createWechatUser(openId));
    user.setTermsAgreedAt(Instant.now()); user.setTermsVersion("2026-10-29"); users.save(user);
    return issue(user);
  }
  public UserView current(String authorization) { AppUser user = user(authorization); return new UserView(user.getId(), user.getNickname()); }
  public Long userId(String authorization) { return user(authorization).getId(); }
  public void logout(String authorization) { user(authorization); tokens.deleteById(authorization.substring(7)); }
  @Transactional
  public void deleteAccount(String authorization) {
    AppUser current = user(authorization);
    Long userId = current.getId();
    for (ChatSession session : sessions.findByUserIdOrderByUpdatedAtDesc(userId)) messages.deleteBySessionId(session.getId());
    sessions.deleteByUserId(userId);
    transactions.deleteByUserId(userId); plans.deleteByUserId(userId); personalities.deleteByUserId(userId);
    // 已进入内容安全处置流程的记录，可能需要依法留存，不随普通账户数据一并删除。
    tokens.deleteByUserId(userId);
    users.delete(current);
  }
  private AppUser createWechatUser(String openId) {
    AppUser user = new AppUser();
    user.setWechatOpenId(openId); user.setNickname("微信用户");
    // 历史字段仅为兼容现有表结构，不作为登录账号或密码使用。
    user.setUsername("wx_" + UUID.randomUUID().toString().replace("-", "").substring(0, 16)); user.setPasswordHash("WECHAT_ONLY");
    return users.save(user);
  }
  private AppUser user(String authorization) {
    if (authorization == null || !authorization.startsWith("Bearer ")) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "请先使用微信登录");
    AuthToken token = tokens.findById(authorization.substring(7)).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录已过期，请重新登录"));
    if (token.getExpiresAt().isBefore(Instant.now())) { tokens.delete(token); throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "登录已过期，请重新登录"); }
    return users.findById(token.getUserId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "用户不存在"));
  }
  private AuthView issue(AppUser user) {
    byte[] bytes = new byte[48]; new SecureRandom().nextBytes(bytes); String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    AuthToken token = new AuthToken(); token.setToken(raw); token.setUserId(user.getId()); token.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS)); tokens.save(token);
    return new AuthView(raw, user.getId(), user.getNickname());
  }
}
