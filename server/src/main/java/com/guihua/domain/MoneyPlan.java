package com.guihua.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "money_plans")
public class MoneyPlan {
  @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
  @Column(nullable = false) private Long userId;
  @Column(nullable = false) private String name;
  @Column(nullable = false, precision = 14, scale = 2) private BigDecimal budget;
  @Column(nullable = false, precision = 14, scale = 2) private BigDecimal spent = BigDecimal.ZERO;
  private LocalDate deadline;
  private String color = "sage";
  public Long getId() { return id; } public Long getUserId() { return userId; } public void setUserId(Long userId) { this.userId = userId; }
  public String getName() { return name; } public void setName(String name) { this.name = name; }
  public BigDecimal getBudget() { return budget; } public void setBudget(BigDecimal budget) { this.budget = budget; }
  public BigDecimal getSpent() { return spent; } public void setSpent(BigDecimal spent) { this.spent = spent; }
  public LocalDate getDeadline() { return deadline; } public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
  public String getColor() { return color; } public void setColor(String color) { this.color = color; }
}
