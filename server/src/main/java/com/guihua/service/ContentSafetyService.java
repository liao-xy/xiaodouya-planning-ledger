package com.guihua.service;

import com.guihua.domain.SafetyEvent;
import com.guihua.domain.SafetyReport;
import com.guihua.repository.SafetyEventRepository;
import com.guihua.repository.SafetyReportRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;
import java.util.regex.Pattern;

/** MVP 阶段的本地前置拦截；正式运营应叠加人工值守和经评估的内容安全服务。 */
@Service
public class ContentSafetyService {
  private final SafetyEventRepository events;
  private final SafetyReportRepository reports;
  private static final List<Pattern> HIGH_RISK_PATTERNS = List.of(
      Pattern.compile("(如何|怎么|帮我|教程|步骤|渠道|方法).{0,30}(洗钱|诈骗|赌博|私彩|制毒|炸弹|黑客攻击|盗号)", Pattern.CASE_INSENSITIVE),
      Pattern.compile("(出售|购买|交易).{0,30}(毒品|枪支|身份证|银行卡|账号)", Pattern.CASE_INSENSITIVE)
  );
  public ContentSafetyService(SafetyEventRepository events, SafetyReportRepository reports) { this.events = events; this.reports = reports; }
  public void assertSafeForAssistant(Long userId, String content) {
    if (content == null) return;
    if (HIGH_RISK_PATTERNS.stream().anyMatch(pattern -> pattern.matcher(content).find())) {
      SafetyEvent event = new SafetyEvent(); event.setUserId(userId); event.setScene("ASSISTANT_INPUT"); event.setRiskType("HIGH_RISK_REQUEST"); event.setContentExcerpt(excerpt(content)); events.save(event);
      throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "该内容可能涉及违法或高风险活动，已停止发送。请仅就合法的个人记账和资金规划提问。");
    }
  }
  public void submitReport(Long userId, String content) {
    SafetyReport report = new SafetyReport(); report.setUserId(userId); report.setContent(content.trim()); reports.save(report);
  }
  private String excerpt(String content) { return content.substring(0, Math.min(600, content.length())); }
}
