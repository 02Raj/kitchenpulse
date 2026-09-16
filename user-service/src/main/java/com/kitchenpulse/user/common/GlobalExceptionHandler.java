package com.kitchenpulse.user.common;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	ResponseEntity<Map<String, String>> handle(ApiException ex) {
		return ResponseEntity.status(ex.status()).body(Map.of("message", ex.getMessage()));
	}
}
