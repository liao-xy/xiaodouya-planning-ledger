package com.guihua.api;

import com.guihua.api.ApiDtos.DailyQuoteView;
import com.guihua.service.DailyQuoteService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/daily-quote")
@CrossOrigin
public class DailyQuoteController {
  private final DailyQuoteService dailyQuote;
  public DailyQuoteController(DailyQuoteService dailyQuote) { this.dailyQuote = dailyQuote; }
  @GetMapping public DailyQuoteView today() { return dailyQuote.today(); }
}
