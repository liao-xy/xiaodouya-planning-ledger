package com.guihua.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@Component
public class WechatIdentityClient {
  private static final Logger log = LoggerFactory.getLogger(WechatIdentityClient.class);
  private final ObjectMapper objectMapper;

  @Value("${app.wechat.app-id:}") private String appId;
  @Value("${app.wechat.app-secret:}") private String appSecret;

  public WechatIdentityClient(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public String openIdFor(String code) {
    if (appId.isBlank() || appSecret.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "服务端尚未配置微信 AppID/AppSecret");
    try {
      String responseBody = RestClient.builder().baseUrl("https://api.weixin.qq.com").build().get().uri(builder -> builder.path("/sns/jscode2session")
          .queryParam("appid", appId).queryParam("secret", appSecret).queryParam("js_code", code).queryParam("grant_type", "authorization_code").build()).retrieve().body(String.class);
      Map<?, ?> response = objectMapper.readValue(responseBody, Map.class);
      Object openId = response == null ? null : response.get("openid");
      if (openId == null || openId.toString().isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "微信身份验证失败，请重试");
      return openId.toString();
    } catch (ResponseStatusException exception) {
      throw exception;
    } catch (Exception exception) {
      // Do not log the authorization code or AppSecret: both are sensitive credentials.
      log.warn("微信 code 换取 OpenID 失败，异常类型：{}，原因：{}",
          exception.getClass().getSimpleName(), exception.getMessage());
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "微信身份服务暂不可用，请稍后重试");
    }
  }
}
