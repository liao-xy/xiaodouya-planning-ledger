package com.guihua.api;

import com.guihua.domain.FlowType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public final class ApiDtos {
  private ApiDtos() {}
  public record PlanRequest(@NotBlank String name, @NotNull @DecimalMin("0.01") BigDecimal budget, LocalDate deadline, String color) {}
  public record TransactionRequest(@NotNull FlowType type, @NotBlank String category, @NotNull @DecimalMin("0.01") BigDecimal amount, Long planId, String note, @NotNull LocalDate occurredOn) {}
  /**
   * balance 是当前全部账单计算出的钱包余额；plannedRemaining 是已分配但尚未使用的金额；
   * unallocated 才是可以继续分配或不归入计划消费的钱。
   */
  public record DashboardResponse(BigDecimal income, BigDecimal expense, BigDecimal balance,
                                  BigDecimal walletBalance, BigDecimal plannedRemaining, BigDecimal unallocated,
                                  List<CategoryAmount> categoryBreakdown, List<DayAmount> dailyExpense, List<PlanSummary> plans) {}
  public record CategoryAmount(String category, BigDecimal amount) {}
  public record DayAmount(LocalDate date, BigDecimal amount) {}
  public record PlanSummary(Long id, String name, BigDecimal budget, BigDecimal spent, BigDecimal remaining, LocalDate deadline, String color) {}
  public record ChatRequest(Long sessionId, @NotBlank String question) {}
  public record ChatReply(Long sessionId, String answer, String evidence) {}
  public record ChatSessionView(Long id, String title, List<ChatMessageView> messages) {}
  public record ChatMessageView(Long id, String role, String content, String evidence) {}
  public record WechatLoginRequest(@NotBlank String code, @AssertTrue Boolean agreedToTerms) {}
  public record AuthView(String token, Long userId, String nickname) {}
  public record UserView(Long id, String nickname) {}
  public record DailyQuoteView(String content, String source) {}
  public record MoneyPersonalityRequest(@NotNull @Size(min = 8, max = 8) List<@NotNull @Min(0) @Max(2) Integer> answers) {}
  public record MoneyPersonalityView(boolean completed, String code, String title, String badge, String motto, String imageKey, String imageUrl,
                                     int planningScore, int experienceScore, int recordScore,
                                     List<String> evidence, List<String> suggestions, String updatedAt) {}
  public record PersonalityImageRequest(@NotBlank @Size(max = 500) String imageUrl) {}
  public record ComplianceView(String appName, String filingNumber, String filingUrl, boolean filingReady,
                               String operatorName, String serviceEmail, String servicePhone, String termsVersion) {}
  public record SafetyReportRequest(@NotBlank @Size(max = 1000) String content) {}
}
