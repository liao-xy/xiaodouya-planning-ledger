package com.guihua.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class TransactionRecord {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false) private Long userId;
  @Enumerated(EnumType.STRING) @Column(nullable = false) private FlowType type;
  @Column(nullable = false) private String category;
  @Column(nullable = false, precision = 14, scale = 2) private BigDecimal amount;
  private Long planId;
  private String note;
  @Column(nullable = false) private LocalDate occurredOn;
  public Long getId() { return id; } public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
  public FlowType getType() { return type; } public void setType(FlowType type) { this.type = type; }
  public String getCategory() { return category; } public void setCategory(String category) { this.category = category; }
  public BigDecimal getAmount() { return amount; } public void setAmount(BigDecimal amount) { this.amount = amount; }
  public Long getPlanId() { return planId; } public void setPlanId(Long planId) { this.planId = planId; }
  public String getNote() { return note; } public void setNote(String note) { this.note = note; }
  public LocalDate getOccurredOn() { return occurredOn; } public void setOccurredOn(LocalDate occurredOn) { this.occurredOn = occurredOn; }
}
