package com.guihua.config;

import com.guihua.domain.*;
import com.guihua.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import java.math.BigDecimal;
import java.time.LocalDate;

@Configuration
@Profile("!mysql")
public class SeedData {
  @Bean CommandLineRunner seed(MoneyPlanRepository plans, TransactionRepository transactions) {
    return args -> { if (plans.count() > 0) return;
      MoneyPlan daily = plan(1L, "日常生活", "1200", "sage"); MoneyPlan study = plan(1L, "学习成长", "820", "orange"); MoneyPlan travel = plan(1L, "旅行基金", "1000", "green"); plans.save(daily); plans.save(study); plans.save(travel);
      transactions.save(tx(1L, FlowType.INCOME, "工资薪酬", "3500", null, "十月收入", LocalDate.now().withDayOfMonth(1)));
      transactions.save(tx(1L, FlowType.EXPENSE, "餐饮·正餐", "18", daily.getId(), "早餐", LocalDate.now()));
      transactions.save(tx(1L, FlowType.EXPENSE, "交通出行", "4", daily.getId(), "地铁", LocalDate.now().minusDays(1)));
      transactions.save(tx(1L, FlowType.EXPENSE, "学习成长", "126", study.getId(), "备考资料", LocalDate.now().minusDays(2)));
      transactions.save(tx(1L, FlowType.EXPENSE, "娱乐休闲", "35", daily.getId(), "电影", LocalDate.now().minusDays(3)));
      daily.setSpent(new BigDecimal("57")); study.setSpent(new BigDecimal("126")); plans.save(daily); plans.save(study);
    };
  }
  private MoneyPlan plan(Long userId, String name, String budget, String color) { MoneyPlan plan = new MoneyPlan(); plan.setUserId(userId); plan.setName(name); plan.setBudget(new BigDecimal(budget)); plan.setColor(color); return plan; }
  private TransactionRecord tx(Long userId, FlowType type, String category, String amount, Long planId, String note, LocalDate date) { TransactionRecord row = new TransactionRecord(); row.setUserId(userId); row.setType(type); row.setCategory(category); row.setAmount(new BigDecimal(amount)); row.setPlanId(planId); row.setNote(note); row.setOccurredOn(date); return row; }
}
