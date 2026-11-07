package com.guihua.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
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
      if (openId == null || openId.toString().isBlank()) {
        Integer errorCode = integerValue(response == null ? null : response.get("errcode"));
        String errorMessage = safeMessage(response == null ? null : response.get("errmsg"));
        // Do not log responseBody: a successful response contains session_key, which is sensitive.
        log.warn("微信 code 换取 OpenID 未返回 openid，errcode={}，errmsg={}", errorCode, errorMessage);
        throw wechatError(errorCode);
      }
      return openId.toString();
    } catch (ResponseStatusException exception) {
      throw exception;
    } catch (ResourceAccessException exception) {
      // Do not log the authorization code, AppSecret or a raw WeChat response.
      log.warn("调用微信 code 换取 OpenID 网络异常，异常类型：{}", exception.getClass().getSimpleName());
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "暂时无法连接微信登录服务，请稍后重试");
    } catch (RestClientResponseException exception) {
      log.warn("调用微信 code 换取 OpenID 返回 HTTP 异常，状态码={}，异常类型={}",
          exception.getStatusCode().value(), exception.getClass().getSimpleName());
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "微信登录服务暂不可用，请稍后重试");
    } catch (Exception exception) {
      // Do not log the authorization code or AppSecret: both are sensitive credentials.
      log.warn("微信 code 换取 OpenID 处理异常，异常类型：{}", exception.getClass().getSimpleName());
      throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "微信身份服务暂不可用，请稍后重试");
    }
  }

  private ResponseStatusException wechatError(Integer errorCode) {
    if (errorCode == null) return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "微信身份验证失败，请返回后重新登录");
    return switch (errorCode) {
      case 40013, 40125 -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "微信登录配置异常，请联系小兜芽客服");
      case 40029, 40163 -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "微信登录凭证已失效，请返回后重新点击登录");
      case 40226 -> new ResponseStatusException(HttpStatus.FORBIDDEN, "当前微信账号暂时无法完成登录，请稍后重试");
      case 45011 -> new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "登录请求较多，请稍后再试");
      case -1 -> new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "微信服务繁忙，请稍后重试");
      default -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "微信身份验证失败，请稍后重试");
    };
  }

  private Integer integerValue(Object value) {
    if (value instanceof Number number) return number.intValue();
    if (value == null) return null;
    try { return Integer.valueOf(value.toString()); } catch (NumberFormatException ignored) { return null; }
  }

  private String safeMessage(Object value) {
    if (value == null) return "<empty>";
    String message = value.toString().replaceAll("[\\r\\n]", " ").trim();
    return message.length() > 240 ? message.substring(0, 240) : message;
  }
}
