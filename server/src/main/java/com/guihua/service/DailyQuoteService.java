package com.guihua.service;

import com.guihua.api.ApiDtos.DailyQuoteView;
import com.guihua.domain.DailyQuote;
import com.guihua.repository.DailyQuoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Service
public class DailyQuoteService {
  private static final ZoneId SHANGHAI = ZoneId.of("Asia/Shanghai");
  private static final List<String> FALLBACKS = List.of(
    "今天先把眼前的一步走稳，明天会自己展开。", "小小的记录，也是在认真照顾生活。",
    "不必急着完美，清楚地前进就很好。", "给重要的事留一份余地，也留一份心安。",
    "你在安排生活，生活也会慢慢回应你。", "每一次克制，都是在靠近更想要的自由。",
    "慢一点没关系，方向清楚就很珍贵。", "把今天过好，就是很了不起的计划。"
  );
  private final DailyQuoteRepository quotes;
  private final RestClient hitokoto = RestClient.builder().baseUrl("https://v1.hitokoto.cn").build();
  public DailyQuoteService(DailyQuoteRepository quotes) { this.quotes = quotes; }

  public DailyQuoteView today() {
    LocalDate date = LocalDate.now(SHANGHAI);
    return quotes.findById(date).map(this::view).orElseGet(() -> fetchAndCache(date));
  }
  private DailyQuoteView fetchAndCache(LocalDate date) {
    String content = null, source = null;
    try {
      Map<?, ?> response = hitokoto.get().uri("/?c=d&c=i").retrieve().body(Map.class);
      if (response != null && response.get("hitokoto") instanceof String text && !text.isBlank() && text.length() <= 100) {
        content = text.trim();
        source = response.get("from") instanceof String from && !from.isBlank() ? from.trim() : "一言";
      }
    } catch (Exception ignored) { /* 网络不可用时使用本地原创备选。 */ }
    if (content == null) { content = FALLBACKS.get(date.getDayOfYear() % FALLBACKS.size()); source = "小兜芽"; }
    DailyQuote saved = quotes.save(new DailyQuote(date, content, source));
    return view(saved);
  }
  private DailyQuoteView view(DailyQuote quote) { return new DailyQuoteView(quote.getContent(), quote.getSource()); }
}
