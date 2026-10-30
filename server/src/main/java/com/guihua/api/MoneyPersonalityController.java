package com.guihua.api;

import com.guihua.api.ApiDtos.MoneyPersonalityRequest;
import com.guihua.api.ApiDtos.MoneyPersonalityView;
import com.guihua.api.ApiDtos.PersonalityImageRequest;
import com.guihua.service.AuthService;
import com.guihua.service.MoneyPersonalityService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/money-personality")
@CrossOrigin
public class MoneyPersonalityController {
  private final MoneyPersonalityService personalities;
  private final AuthService auth;
  public MoneyPersonalityController(MoneyPersonalityService personalities, AuthService auth) { this.personalities = personalities; this.auth = auth; }
  @GetMapping public MoneyPersonalityView current(@RequestHeader(value = "Authorization", required = false) String authorization) { return personalities.current(auth.userId(authorization)); }
  @PostMapping("/assess") public MoneyPersonalityView assess(@RequestHeader(value = "Authorization", required = false) String authorization, @Valid @RequestBody MoneyPersonalityRequest request) { return personalities.assess(auth.userId(authorization), request); }
  @PutMapping("/image") public MoneyPersonalityView image(@RequestHeader(value = "Authorization", required = false) String authorization, @Valid @RequestBody PersonalityImageRequest request) { return personalities.updateImage(auth.userId(authorization), request); }
}
