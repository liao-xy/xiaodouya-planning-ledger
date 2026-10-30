package com.guihua.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;
import java.util.LinkedHashMap;
import java.util.Map;

/** 让小程序获得可读的业务错误，而不是笼统的“请求失败”。 */
@RestControllerAdvice
public class ApiExceptionHandler {
  @ExceptionHandler(ResponseStatusException.class)
  public ResponseEntity<Map<String, String>> responseStatus(ResponseStatusException exception) {
    return response(exception.getStatusCode().value(), exception.getReason() == null ? "请求无法完成，请稍后重试" : exception.getReason());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> validation(MethodArgumentNotValidException exception) {
    String message = exception.getBindingResult().getFieldErrors().stream().findFirst()
        .map(error -> switch (error.getField()) {
          case "name" -> "请填写计划名称";
          case "budget", "amount" -> "请输入大于 0 的金额";
          case "category" -> "请选择分类";
          case "occurredOn" -> "请选择日期";
          default -> "请检查填写内容";
        }).orElse("请检查填写内容");
    return response(HttpStatus.BAD_REQUEST.value(), message);
  }

  private ResponseEntity<Map<String, String>> response(int status, String message) {
    Map<String, String> body = new LinkedHashMap<>(); body.put("message", message);
    return ResponseEntity.status(status).body(body);
  }
}
