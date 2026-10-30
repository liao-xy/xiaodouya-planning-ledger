package com.guihua.api;

import com.guihua.api.ApiDtos.*;
import com.guihua.domain.*;
import com.guihua.service.AuthService;
import com.guihua.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;
import java.util.List;

@RestController @RequestMapping("/api") @CrossOrigin
public class LedgerController {
  private final LedgerService ledger;
  private final AuthService auth;
  public LedgerController(LedgerService ledger, AuthService auth) { this.ledger = ledger; this.auth = auth; }
  @GetMapping("/dashboard") public DashboardResponse dashboard(@RequestHeader(value = "Authorization", required = false) String authorization, @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) { return ledger.dashboard(auth.userId(authorization), month == null ? YearMonth.now() : month); }
  @GetMapping("/plans") public List<MoneyPlan> plans(@RequestHeader(value = "Authorization", required = false) String authorization) { return ledger.plans(auth.userId(authorization)); }
  @PostMapping("/plans") @ResponseStatus(HttpStatus.CREATED) public MoneyPlan createPlan(@RequestHeader(value = "Authorization", required = false) String authorization, @Valid @RequestBody PlanRequest request) { return ledger.createPlan(auth.userId(authorization), request); }
  @PutMapping("/plans/{id}") public MoneyPlan updatePlan(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable Long id, @Valid @RequestBody PlanRequest request) { return ledger.updatePlan(auth.userId(authorization), id, request); }
  @DeleteMapping("/plans/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deletePlan(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable Long id) { ledger.deletePlan(auth.userId(authorization), id); }
  @GetMapping("/transactions") public List<TransactionRecord> transactions(@RequestHeader(value = "Authorization", required = false) String authorization, @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) { return ledger.transactions(auth.userId(authorization), month); }
  @PostMapping("/transactions") @ResponseStatus(HttpStatus.CREATED) public TransactionRecord createTransaction(@RequestHeader(value = "Authorization", required = false) String authorization, @Valid @RequestBody TransactionRequest request) { return ledger.createTransaction(auth.userId(authorization), request); }
  @PutMapping("/transactions/{id}") public TransactionRecord updateTransaction(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable Long id, @Valid @RequestBody TransactionRequest request) { return ledger.updateTransaction(auth.userId(authorization), id, request); }
  @DeleteMapping("/transactions/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteTransaction(@RequestHeader(value = "Authorization", required = false) String authorization, @PathVariable Long id) { ledger.deleteTransaction(auth.userId(authorization), id); }
}
