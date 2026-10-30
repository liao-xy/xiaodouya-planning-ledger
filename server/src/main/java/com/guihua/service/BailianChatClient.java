package com.guihua.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.*;

@Component
public class BailianChatClient {
  @Value("${app.ai.bailian-api-key:}") private String apiKey;
  @Value("${app.ai.base-url}") private String baseUrl;
  @Value("${app.ai.model}") private String model;

  public Optional<String> reply(String systemPrompt, List<Map<String, String>> history) {
    if (apiKey == null || apiKey.isBlank()) return Optional.empty();
    List<Map<String, String>> messages = new ArrayList<>(); messages.add(Map.of("role", "system", "content", systemPrompt)); messages.addAll(history);
    Map<String, Object> body = Map.of("model", model, "temperature", 0.2, "messages", messages);
    try {
      Map<?, ?> response = RestClient.builder().baseUrl(baseUrl).defaultHeader("Authorization", "Bearer " + apiKey).build()
          .post().uri("/chat/completions").body(body).retrieve().body(Map.class);
      if (response == null || !(response.get("choices") instanceof List<?> choices) || choices.isEmpty() || !(choices.getFirst() instanceof Map<?, ?> choice) || !(choice.get("message") instanceof Map<?, ?> message)) return Optional.empty();
      Object content = message.get("content"); return content == null ? Optional.empty() : Optional.of(content.toString());
    } catch (Exception ignored) { return Optional.empty(); }
  }
}
