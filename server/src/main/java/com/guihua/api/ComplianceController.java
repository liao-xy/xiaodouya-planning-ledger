package com.guihua.api;

import com.guihua.api.ApiDtos.*;
import com.guihua.service.AuthService;
import com.guihua.service.ContentSafetyService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class ComplianceController {
  private final AuthService auth;
  private final ContentSafetyService safety;
  @Value("${app.compliance.filing-number:}") private String filingNumber;
  @Value("${app.compliance.operator-name:}") private String operatorName;
  @Value("${app.compliance.service-email:}") private String serviceEmail;
  @Value("${app.compliance.service-phone:}") private String servicePhone;
  public ComplianceController(AuthService auth, ContentSafetyService safety) { this.auth = auth; this.safety = safety; }
  @GetMapping("/public/compliance")
  public ComplianceView compliance() {
    boolean ready = filingNumber != null && !filingNumber.isBlank();
    return new ComplianceView("小兜芽", ready ? filingNumber.trim() : "", "https://beian.miit.gov.cn", ready,
        value(operatorName), value(serviceEmail), value(servicePhone), "2026-10-29");
  }
  @PostMapping("/safety/reports") @ResponseStatus(HttpStatus.CREATED)
  public void report(@RequestHeader(value = "Authorization", required = false) String authorization,
                     @Valid @RequestBody SafetyReportRequest request) {
    safety.submitReport(auth.userId(authorization), request.content());
  }
  private String value(String value) { return value == null ? "" : value.trim(); }
}
