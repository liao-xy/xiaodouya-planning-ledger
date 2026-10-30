package com.guihua.service;

import com.guihua.api.ApiDtos.*;
import com.guihua.domain.*;
import com.guihua.repository.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class LedgerService {
  private final MoneyPlanRepository plans;
  private final TransactionRepository transactions;
  public LedgerService(MoneyPlanRepository plans, TransactionRepository transactions) { this.plans = plans; this.transactions = transactions; }

  public List<MoneyPlan> plans(Long userId) { return plans.findByUserIdOrderByIdDesc(userId); }
  @Transactional public MoneyPlan createPlan(Long userId, PlanRequest request) {
    requireAllocatable(userId, request.budget(), null);
    MoneyPlan plan = new MoneyPlan(); plan.setUserId(userId); plan.setName(request.name()); plan.setBudget(request.budget()); plan.setDeadline(request.deadline());
    if (request.color() != null && !request.color().isBlank()) plan.setColor(request.color());
    return plans.save(plan);
  }
  @Transactional public MoneyPlan updatePlan(Long userId, Long id, PlanRequest request) {
    MoneyPlan plan = planFor(userId, id);
    if (request.budget().compareTo(plan.getSpent()) < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "计划金额不能低于已用金额");
    requireAllocatable(userId, request.budget().subtract(plan.getSpent()), id);
    plan.setName(request.name()); plan.setBudget(request.budget()); plan.setDeadline(request.deadline());
    if (request.color() != null && !request.color().isBlank()) plan.setColor(request.color()); return plans.save(plan);
  }
  @Transactional public void deletePlan(Long userId, Long id) {
    MoneyPlan plan = planFor(userId, id);
    // 保留历史账单，只解除它和计划的关联，避免出现悬空 planId。
    List<TransactionRecord> linked = transactions.findByUserIdOrderByOccurredOnDescIdDesc(userId).stream()
      .filter(row -> id.equals(row.getPlanId())).toList();
    linked.forEach(row -> row.setPlanId(null));
    transactions.saveAll(linked);
    plans.delete(plan);
  }
  public List<TransactionRecord> transactions(Long userId, YearMonth month) {
    if (month == null) return transactions.findByUserIdOrderByOccurredOnDescIdDesc(userId);
    return transactions.findByUserIdAndOccurredOnBetweenOrderByOccurredOnDescIdDesc(userId, month.atDay(1), month.atEndOfMonth());
  }
  public List<TransactionRecord> recentTransactions(Long userId) { return transactions.findTop200ByUserIdOrderByOccurredOnDescIdDesc(userId); }
  @Transactional public TransactionRecord createTransaction(Long userId, TransactionRequest request) {
    requirePlanOwnership(userId, request.planId());
    validateExpenseAgainstAvailableMoney(userId, request, null);
    TransactionRecord row = toTransaction(userId, new TransactionRecord(), request); transactions.save(row); recalculatePlan(userId, request.planId()); return row;
  }
  @Transactional public TransactionRecord updateTransaction(Long userId, Long id, TransactionRequest request) {
    TransactionRecord row = transactionFor(userId, id); Long previousPlanId = row.getPlanId(); requirePlanOwnership(userId, request.planId());
    validateExpenseAgainstAvailableMoney(userId, request, row);
    toTransaction(userId, row, request); transactions.save(row); recalculatePlan(userId, previousPlanId); recalculatePlan(userId, request.planId()); return row;
  }
  @Transactional public void deleteTransaction(Long userId, Long id) { TransactionRecord row = transactionFor(userId, id); Long planId = row.getPlanId(); transactions.delete(row); recalculatePlan(userId, planId); }
  public DashboardResponse dashboard(Long userId, YearMonth month) {
    List<TransactionRecord> rows = transactions(userId, month);
    BigDecimal income = sum(rows, FlowType.INCOME), expense = sum(rows, FlowType.EXPENSE);
    Map<String, BigDecimal> categories = rows.stream().filter(row -> row.getType() == FlowType.EXPENSE).collect(Collectors.groupingBy(TransactionRecord::getCategory, LinkedHashMap::new, Collectors.reducing(BigDecimal.ZERO, TransactionRecord::getAmount, BigDecimal::add)));
    Map<LocalDate, BigDecimal> daily = rows.stream().filter(row -> row.getType() == FlowType.EXPENSE).collect(Collectors.groupingBy(TransactionRecord::getOccurredOn, TreeMap::new, Collectors.reducing(BigDecimal.ZERO, TransactionRecord::getAmount, BigDecimal::add)));
    List<PlanSummary> planSummaries = plans(userId).stream().map(this::planSummary).toList();
    BigDecimal walletBalance = walletBalance(userId);
    BigDecimal plannedRemaining = plannedRemaining(userId, null);
    BigDecimal unallocated = walletBalance.subtract(plannedRemaining);
    return new DashboardResponse(income, expense, income.subtract(expense), walletBalance, plannedRemaining, unallocated,
      categories.entrySet().stream().map(item -> new CategoryAmount(item.getKey(), item.getValue())).toList(),
      daily.entrySet().stream().map(item -> new DayAmount(item.getKey(), item.getValue())).toList(), planSummaries);
  }
  public PlanSummary planSummary(MoneyPlan plan) { return new PlanSummary(plan.getId(), plan.getName(), plan.getBudget(), plan.getSpent(), plan.getBudget().subtract(plan.getSpent()), plan.getDeadline(), plan.getColor()); }
  private BigDecimal sum(List<TransactionRecord> rows, FlowType type) { return rows.stream().filter(row -> row.getType() == type).map(TransactionRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add); }
  private TransactionRecord toTransaction(Long userId, TransactionRecord target, TransactionRequest request) { target.setUserId(userId); target.setType(request.type()); target.setCategory(request.category()); target.setAmount(request.amount()); target.setPlanId(request.planId()); target.setNote(request.note()); target.setOccurredOn(request.occurredOn()); return target; }
  private void recalculatePlan(Long userId, Long planId) {
    if (planId == null) return; MoneyPlan plan = planFor(userId, planId);
    BigDecimal spent = transactions.findByUserIdOrderByOccurredOnDescIdDesc(userId).stream().filter(row -> planId.equals(row.getPlanId()) && row.getType() == FlowType.EXPENSE).map(TransactionRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    plan.setSpent(spent); plans.save(plan);
  }
  private BigDecimal walletBalance(Long userId) {
    List<TransactionRecord> rows = transactions.findByUserIdOrderByOccurredOnDescIdDesc(userId);
    return sum(rows, FlowType.INCOME).subtract(sum(rows, FlowType.EXPENSE));
  }
  private BigDecimal plannedRemaining(Long userId, Long excludedPlanId) {
    return plans(userId).stream().filter(plan -> !plan.getId().equals(excludedPlanId))
      .map(plan -> plan.getBudget().subtract(plan.getSpent()).max(BigDecimal.ZERO))
      .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
  private void requireAllocatable(Long userId, BigDecimal amountToAllocate, Long updatingPlanId) {
    BigDecimal free = walletBalance(userId).subtract(plannedRemaining(userId, updatingPlanId));
    if (amountToAllocate.compareTo(free) > 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
      "可分配余额不足：当前可分配 ¥" + free.max(BigDecimal.ZERO).toPlainString());
  }
  private void validateExpenseAgainstAvailableMoney(Long userId, TransactionRequest request, TransactionRecord previous) {
    if (request.type() != FlowType.EXPENSE) return;
    if (request.planId() != null) {
      MoneyPlan plan = planFor(userId, request.planId());
      BigDecimal alreadySpent = plan.getSpent();
      if (previous != null && request.planId().equals(previous.getPlanId()) && previous.getType() == FlowType.EXPENSE) {
        alreadySpent = alreadySpent.subtract(previous.getAmount());
      }
      if (request.amount().compareTo(plan.getBudget().subtract(alreadySpent)) > 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
        "该计划的可用金额不足，请调整计划金额或归入其他用途");
      return;
    }
    BigDecimal free = walletBalance(userId).subtract(plannedRemaining(userId, null));
    if (previous != null) {
      if (previous.getType() == FlowType.INCOME) free = free.subtract(previous.getAmount());
      if (previous.getType() == FlowType.EXPENSE && previous.getPlanId() == null) free = free.add(previous.getAmount());
    }
    if (request.amount().compareTo(free) > 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
      "未分配余额不足，请先调整资金计划或把该笔消费归入对应计划");
  }
  private void requirePlanOwnership(Long userId, Long planId) { if (planId != null) planFor(userId, planId); }
  private MoneyPlan planFor(Long userId, Long id) { MoneyPlan plan = plans.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "资金计划不存在")); if (!plan.getUserId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问该资金计划"); return plan; }
  private TransactionRecord transactionFor(Long userId, Long id) { TransactionRecord row = transactions.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "账单不存在")); if (!row.getUserId().equals(userId)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "无权访问该账单"); return row; }
}
