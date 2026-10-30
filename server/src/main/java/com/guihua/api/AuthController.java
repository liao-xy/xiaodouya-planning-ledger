package com.guihua.api;

import com.guihua.api.ApiDtos.*;
import com.guihua.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @CrossOrigin
public class AuthController {
  private final AuthService auth;
  public AuthController(AuthService auth) { this.auth = auth; }
  @PostMapping("/wechat-login") public AuthView wechatLogin(@Valid @RequestBody WechatLoginRequest request) { return auth.wechatLogin(request); }
  @GetMapping("/me") public UserView me(@RequestHeader(value = "Authorization", required = false) String authorization) { return auth.current(authorization); }
  @PostMapping("/logout") @ResponseStatus(HttpStatus.NO_CONTENT) public void logout(@RequestHeader(value = "Authorization", required = false) String authorization) { auth.logout(authorization); }
  @DeleteMapping("/account") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteAccount(@RequestHeader(value = "Authorization", required = false) String authorization) { auth.deleteAccount(authorization); }
}
