package com.guihua.service;

import com.guihua.api.ApiDtos.*;
import com.guihua.domain.*;
import com.guihua.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WalletAssistantService {
  private final ChatSessionRepository sessions; private final ChatMessageRepository messages; private final LedgerService ledger; private final BailianChatClient bailian; private final MoneyPersonalityService personalities; private final ContentSafetyService safety;
  public WalletAssistantService(ChatSessionRepository sessions, ChatMessageRepository messages, LedgerService ledger, BailianChatClient bailian, MoneyPersonalityService personalities, ContentSafetyService safety) { this.sessions = sessions; this.messages = messages; this.ledger = ledger; this.bailian = bailian; this.personalities = personalities; this.safety = safety; }
  public List<ChatSessionView> listSessions(Long userId) { return sessions.findByUserIdOrderByUpdatedAtDesc(userId).stream().map(this::view).toList(); }
  @Transactional public ChatSessionView createSession(Long userId) { ChatSession session = new ChatSession(); session.setUserId(userId); session.setTitle("新对话"); sessions.save(session); return view(session); }
  @Transactional public ChatReply reply(Long userId, ChatRequest request) {
    safety.assertSafeForAssistant(userId, request.question());
    ChatSession session = request.sessionId() == null ? create(userId) : sessionFor(userId, request.sessionId());
    saveMessage(session.getId(), "user", request.question(), null);
    DashboardResponse dashboard = ledger.dashboard(userId, YearMonth.now());
    MoneyPersonalityView personality = personalities.current(userId);
    String evidence = evidence(userId, dashboard, personality);
    List<java.util.Map<String, String>> history = messages.findBySessionIdOrderByCreatedAtAsc(session.getId()).stream().limit(16).map(item -> java.util.Map.of("role", item.getRole(), "content", item.getContent())).toList();
    String answer = bailian.reply(systemPrompt(evidence), history).orElseGet(() -> localAnswer(request.question(), dashboard, personality));
    saveMessage(session.getId(), "assistant", answer, evidence);
    if ("新对话".equals(session.getTitle())) { session.setTitle(request.question().substring(0, Math.min(12, request.question().length()))); }
    session.setUpdatedAt(Instant.now()); sessions.save(session);
    return new ChatReply(session.getId(), answer, evidence);
  }
  private ChatSession create(Long userId) { ChatSession session = new ChatSession(); session.setUserId(userId); session.setTitle("新对话"); return sessions.save(session); }
  private ChatMessage saveMessage(Long sessionId, String role, String content, String evidence) { ChatMessage message = new ChatMessage(); message.setSessionId(sessionId); message.setRole(role); message.setContent(content); message.setEvidence(evidence); return messages.save(message); }
  private String localAnswer(String question, DashboardResponse dashboard, MoneyPersonalityView personality) {
    String q = question == null ? "" : question;
    if (q.contains("钱格") || q.contains("人格") || q.contains("测试")) {
      if (!personality.completed()) return "你还没有完成钱格测试。完成 8 道题后，我才能结合最新测试结果和真实账单解读你的钱格。";
      return "你的最新钱格是“" + personality.title() + "”（" + personality.badge() + "）。规划感 " + personality.planningScore() + " 分、体验感 " + personality.experienceScore() + " 分、记录力 " + personality.recordScore() + " 分。当前钱包余额 " + dashboard.walletBalance() + " 元，未分配 " + dashboard.unallocated() + " 元。" + String.join(" ", personality.suggestions());
    }
    if (q.contains("日常") || q.contains("还能") || q.contains("预算")) {
      PlanSummary plan = dashboard.plans().stream().filter(item -> item.name().contains("日常")).findFirst().orElse(null);
      if (plan != null) return "日常生活计划还剩 " + plan.remaining() + " 元。建议结合下一次收入日期，按剩余天数拆分成每日可花额度。";
    }
    if (q.contains("最高") || q.contains("最多") || q.contains("分类")) {
      return dashboard.categoryBreakdown().stream().max(Comparator.comparing(CategoryAmount::amount)).map(item -> "当前记录中支出最高的是“" + item.category() + "”，金额为 " + item.amount() + " 元。这个结论只基于本月已记录账单。").orElse("本月还没有支出账单，暂时无法比较消费分类。");
    }
    if (q.contains("计划") || q.contains("旅行")) {
      return dashboard.plans().stream().filter(item -> q.contains("旅行") ? item.name().contains("旅行") : true).findFirst().map(item -> item.name() + "预算 " + item.budget() + " 元，已用 " + item.spent() + " 元，剩余 " + item.remaining() + " 元。").orElse("当前没有匹配的资金计划。");
    }
    return "我会严格依据你的账本回答。当前本月收入 " + dashboard.income() + " 元、支出 " + dashboard.expense() + " 元、结余 " + dashboard.balance() + " 元。你可以继续问我分类消费、预算余额或具体计划。";
  }
  private String evidence(Long userId, DashboardResponse dashboard, MoneyPersonalityView personality) {
    List<TransactionRecord> records = ledger.recentTransactions(userId);
    List<TransactionRecord> allRecords = ledger.transactions(userId, null);
    String categories = dashboard.categoryBreakdown().isEmpty() ? "无支出分类记录" : dashboard.categoryBreakdown().stream()
        .map(item -> item.category() + " " + item.amount() + " 元（" + percent(item.amount(), dashboard.expense()) + "）")
        .collect(Collectors.joining("；"));
    String plans = dashboard.plans().isEmpty() ? "无资金计划" : dashboard.plans().stream()
        .map(item -> item.name() + "：预算 " + item.budget() + " 元，已用 " + item.spent() + " 元，剩余 " + item.remaining() + " 元" + (item.deadline() == null ? "" : "，截止 " + item.deadline()))
        .collect(Collectors.joining("；"));
    String monthly = allRecords.stream().collect(Collectors.groupingBy(row -> YearMonth.from(row.getOccurredOn()), Collectors.toList())).entrySet().stream()
        .sorted(Map.Entry.<YearMonth, List<TransactionRecord>>comparingByKey().reversed()).limit(6)
        .map(item -> monthSummary(item.getKey(), item.getValue())).collect(Collectors.joining("；"));
    String recent = records.stream().limit(60).map(row -> row.getOccurredOn() + " " + (row.getType() == FlowType.INCOME ? "收入" : "支出") + " " + row.getCategory() + " " + row.getAmount() + " 元" + (row.getNote() == null || row.getNote().isBlank() ? "" : "（" + row.getNote() + "）")).collect(Collectors.joining("；"));
    String personalitySnapshot = !personality.completed() ? "用户尚未完成钱格测试。" : "最新钱格测试（更新时间 " + personality.updatedAt() + "）：结果“" + personality.title() + "”，编码 " + personality.code() + "；规划感 " + personality.planningScore() + " 分，体验感 " + personality.experienceScore() + " 分，记录力 " + personality.recordScore() + " 分。测试的数据依据：" + String.join("；", personality.evidence()) + "。测试建议：" + String.join("；", personality.suggestions()) + "。";
    return "服务器为当前用户实时生成的数据快照（不可添加未记录事实）：\n"
        + "当前钱包（全部历史账单累计）：" + dashboard.walletBalance() + " 元；已分配但未用：" + dashboard.plannedRemaining() + " 元；未分配可自由使用：" + dashboard.unallocated() + " 元。\n"
        + "本月：收入 " + dashboard.income() + " 元；支出 " + dashboard.expense() + " 元；结余 " + dashboard.balance() + " 元。\n"
        + "本月支出分类及占比：" + categories + "。\n"
        + "资金计划：" + plans + "。\n"
        + "钱格测试：" + personalitySnapshot + "\n"
        + "近 6 个有记录月份（按全部历史账单汇总）：" + (monthly.isBlank() ? "无" : monthly) + "。\n"
        + "最近 " + records.size() + " 笔真实账单（最多传入 200 笔，以下展示最近 60 笔）：" + (recent.isBlank() ? "无" : recent) + "。";
  }
  private String monthSummary(YearMonth month, List<TransactionRecord> rows) {
    BigDecimal income = rows.stream().filter(row -> row.getType() == FlowType.INCOME).map(TransactionRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal expense = rows.stream().filter(row -> row.getType() == FlowType.EXPENSE).map(TransactionRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    return month + " 收入 " + income + " 元、支出 " + expense + " 元、结余 " + income.subtract(expense) + " 元";
  }
  private String percent(BigDecimal amount, BigDecimal total) { return total.signum() == 0 ? "0%" : amount.multiply(BigDecimal.valueOf(100)).divide(total, 1, java.math.RoundingMode.HALF_UP) + "%"; }
  private String systemPrompt(String evidence) { return "你是小兜芽的兜兜 AI。严格只依据以下服务器计算的数据回答，不得虚构未记录的交易、收入、计划、收入日期或用户特征。用户询问钱格测试时，必须优先使用数据快照中标明的“最新钱格测试”，并把钱格分数与真实账本共同解释；不得仅复述测试标签，也不能使用过期会话中的测试结论。回答应先给结论，再列不超过 3 条现实可执行的建议；每条建议要写明金额、分类、计划、测试分数或时间等数据依据。建议应是可选行动，例如设置分类上限、将某笔可支配余额分配到既有计划、暂停非必要支出；避免空泛鼓励。数据不足时直接说明不足。不要提供投资、借贷、保险、医疗或法律建议，也不要替用户作出消费决定。不得生成或协助诈骗、洗钱、赌博、非法交易、暴力伤害、入侵或其他违法有害活动；遇到此类请求应简短拒绝并建议寻求合法途径。\n\n" + evidence; }
  private ChatSessionView view(ChatSession session) { return new ChatSessionView(session.getId(), session.getTitle(), messages.findBySessionIdOrderByCreatedAtAsc(session.getId()).stream().map(item -> new ChatMessageView(item.getId(), item.getRole(), item.getContent(), item.getEvidence())).toList()); }
  private ChatSession sessionFor(Long userId, Long id) { ChatSession session = sessions.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "会话不存在")); if (!session.getUserId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问该会话"); return session; }
}
