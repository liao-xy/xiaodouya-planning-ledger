package com.guihua.service;

import com.guihua.api.ApiDtos.*;
import com.guihua.domain.FlowType;
import com.guihua.domain.MoneyPersonality;
import com.guihua.domain.TransactionRecord;
import com.guihua.repository.MoneyPersonalityRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MoneyPersonalityService {
  private static final Set<String> EXPERIENCE_CATEGORIES = Set.of("娱乐休闲", "旅行度假", "人情礼物", "服饰美容", "零食饮品", "运动健康");
  private final MoneyPersonalityRepository personalities;
  private final LedgerService ledger;
  public MoneyPersonalityService(MoneyPersonalityRepository personalities, LedgerService ledger) { this.personalities = personalities; this.ledger = ledger; }

  public MoneyPersonalityView current(Long userId) {
    return personalities.findByUserId(userId).map(item -> view(item, ledger.dashboard(userId, java.time.YearMonth.now()), ledger.transactions(userId, null))).orElseGet(this::empty);
  }
  @Transactional public MoneyPersonalityView assess(Long userId, MoneyPersonalityRequest request) {
    DashboardResponse dashboard = ledger.dashboard(userId, java.time.YearMonth.now());
    List<TransactionRecord> rows = ledger.transactions(userId, null);
    List<Integer> answers = request.answers();
    int planning = planningScore(answers, dashboard, rows);
    int experience = experienceScore(answers, rows);
    int record = recordScore(answers, rows);
    Result result = resultFor(planning, experience, record);
    MoneyPersonality item = personalities.findByUserId(userId).orElseGet(MoneyPersonality::new);
    if (item.getResultCode() != null && !item.getResultCode().equals(result.code)) item.setImageUrl(null);
    item.setUserId(userId); item.setAnswers(answers.stream().map(String::valueOf).collect(Collectors.joining(",")));
    item.setResultCode(result.code); item.setResultTitle(result.title); item.setPlanningScore(planning); item.setExperienceScore(experience); item.setRecordScore(record); item.setUpdatedAt(Instant.now());
    personalities.save(item);
    return view(item, dashboard, rows);
  }
  @Transactional public MoneyPersonalityView updateImage(Long userId, PersonalityImageRequest request) {
    MoneyPersonality item = personalities.findByUserId(userId).orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND, "请先完成钱格测试"));
    item.setImageUrl(request.imageUrl().trim()); item.setUpdatedAt(Instant.now()); personalities.save(item);
    return view(item, ledger.dashboard(userId, java.time.YearMonth.now()), ledger.transactions(userId, null));
  }
  private MoneyPersonalityView empty() { return new MoneyPersonalityView(false, null, null, null, null, null, null, 0, 0, 0, List.of(), List.of(), null); }
  private int planningScore(List<Integer> a, DashboardResponse d, List<TransactionRecord> rows) {
    double self = (a.get(0) + (2 - a.get(1)) + a.get(2)) / 6.0 * 60;
    long plannedExpense = rows.stream().filter(row -> row.getType() == FlowType.EXPENSE && row.getPlanId() != null).count();
    long expenseCount = rows.stream().filter(row -> row.getType() == FlowType.EXPENSE).count();
    double linkedRate = expenseCount == 0 ? 0.5 : (double) plannedExpense / expenseCount;
    double allocationRate = d.walletBalance().signum() <= 0 ? 0 : d.plannedRemaining().divide(d.walletBalance(), 3, RoundingMode.HALF_UP).doubleValue();
    return clamp((int) Math.round(self + linkedRate * 25 + Math.min(allocationRate, 1) * 15));
  }
  private int experienceScore(List<Integer> a, List<TransactionRecord> rows) {
    double self = (a.get(3) + a.get(4) + a.get(5)) / 6.0 * 60;
    BigDecimal all = rows.stream().filter(row -> row.getType() == FlowType.EXPENSE).map(TransactionRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    BigDecimal experiential = rows.stream().filter(row -> row.getType() == FlowType.EXPENSE && EXPERIENCE_CATEGORIES.contains(row.getCategory())).map(TransactionRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    double share = all.signum() == 0 ? 0.3 : experiential.divide(all, 3, RoundingMode.HALF_UP).doubleValue();
    return clamp((int) Math.round(self + Math.min(share, 1) * 40));
  }
  private int recordScore(List<Integer> a, List<TransactionRecord> rows) {
    double self = (a.get(6) + a.get(7)) / 4.0 * 60;
    long days = rows.stream().map(TransactionRecord::getOccurredOn).filter(day -> !day.isBefore(LocalDate.now().minusDays(29))).distinct().count();
    return clamp((int) Math.round(self + Math.min(days / 12.0, 1) * 40));
  }
  private int clamp(int score) { return Math.max(0, Math.min(score, 100)); }
  private Result resultFor(int planning, int experience, int record) {
    boolean plan = planning >= 60, joy = experience >= 60, track = record >= 60;
    if (plan && !joy && track) return new Result("P-G-R", "预算守护獭", "稳稳拿捏版", "钱要有方向，快乐也要有余量。", "personality-p-g-r");
    if (plan && !joy) return new Result("P-G-S", "佛系金库管理员", "存得明白版", "不声张地攒着，是你对未来的偏爱。", "personality-p-g-s");
    if (plan && joy && track) return new Result("P-E-R", "松弛掌舵人", "快乐有预算版", "会玩不是乱花，是提前给快乐留了座位。", "personality-p-e-r");
    if (plan && joy) return new Result("P-E-S", "灵感行程策展人", "想去就有底气版", "快乐有计划，出发才不慌张。", "personality-p-e-s");
    if (!plan && !joy && track) return new Result("F-G-R", "清醒补给站", "随手记也靠谱版", "不爱复杂规划，但你从不让生活失联。", "personality-f-g-r");
    if (!plan && !joy) return new Result("F-G-S", "安全感建筑师", "慢慢攒也很酷版", "不追热点的你，在给生活打地基。", "personality-f-g-s");
    if (track) return new Result("F-E-R", "快乐预售员", "人生体验优先版", "先把快乐加入购物车，再慢慢研究余额。", "personality-f-e-r");
    return new Result("F-E-S", "随心星球探险家", "灵感消费玩家版", "世界这么大，先去体验再慢慢复盘。", "personality-f-e-s");
  }
  private MoneyPersonalityView view(MoneyPersonality item, DashboardResponse dashboard, List<TransactionRecord> rows) {
    Result result = resultFor(item.getPlanningScore(), item.getExperienceScore(), item.getRecordScore());
    BigDecimal experienceAmount = rows.stream().filter(row -> row.getType() == FlowType.EXPENSE && EXPERIENCE_CATEGORIES.contains(row.getCategory())).map(TransactionRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    long activeDays = rows.stream().map(TransactionRecord::getOccurredOn).filter(day -> !day.isBefore(LocalDate.now().minusDays(29))).distinct().count();
    List<String> evidence = List.of("钱包余额 ¥" + dashboard.walletBalance() + "，其中已分配 ¥" + dashboard.plannedRemaining() + "，未分配 ¥" + dashboard.unallocated(),
      "近 30 天记录了 " + activeDays + " 天；体验型分类累计 ¥" + experienceAmount,
      "本次结果由 8 道自评题与已记录账单共同计算，不会读取未记录的消费。");
    List<String> suggestions = suggestions(item, dashboard);
    return new MoneyPersonalityView(true, result.code, result.title, result.badge + " · 记录力 " + item.getRecordScore(), result.motto, result.imageKey, item.getImageUrl(),
      item.getPlanningScore(), item.getExperienceScore(), item.getRecordScore(), evidence, suggestions, item.getUpdatedAt().toString());
  }
  private List<String> suggestions(MoneyPersonality item, DashboardResponse dashboard) {
    List<String> list = new ArrayList<>();
    if (item.getPlanningScore() < 60) list.add("先从把未分配的 ¥" + dashboard.unallocated().max(BigDecimal.ZERO) + " 划出一小部分开始，不必一次做满所有计划。");
    else list.add("保持计划与实际账单关联；已分配的 ¥" + dashboard.plannedRemaining() + " 可以继续守住对应用途。");
    if (item.getExperienceScore() >= 60) list.add("给娱乐、旅行和礼物等体验型消费设一个可安心使用的月度额度，让快乐不需要事后内疚。");
    else list.add("可以给自己预留一笔小小的“任性额度”，让规划也保留生活感。 ");
    if (item.getRecordScore() < 60) list.add("连续 7 天随手记一笔，先建立节奏，不追求记得完美。");
    return list;
  }
  private record Result(String code, String title, String badge, String motto, String imageKey) {}
}
