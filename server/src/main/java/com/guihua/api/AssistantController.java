package com.guihua.api;
import com.guihua.api.ApiDtos.*;
import com.guihua.service.WalletAssistantService;
import com.guihua.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequestMapping("/api/assistant") @CrossOrigin
public class AssistantController {
  private final WalletAssistantService assistant;
  private final AuthService auth;
  public AssistantController(WalletAssistantService assistant, AuthService auth) { this.assistant = assistant; this.auth = auth; }
  @GetMapping("/sessions") public List<ChatSessionView> sessions(@RequestHeader(value = "Authorization", required = false) String authorization) { return assistant.listSessions(auth.userId(authorization)); }
  @PostMapping("/sessions") @ResponseStatus(HttpStatus.CREATED) public ChatSessionView createSession(@RequestHeader(value = "Authorization", required = false) String authorization) { return assistant.createSession(auth.userId(authorization)); }
  @PostMapping("/chat") public ChatReply chat(@RequestHeader(value = "Authorization", required = false) String authorization, @Valid @RequestBody ChatRequest request) { return assistant.reply(auth.userId(authorization), request); }
}
