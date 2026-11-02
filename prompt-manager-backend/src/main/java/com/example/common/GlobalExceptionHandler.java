package com.example.common;

import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalArgumentException.class)
    public org.springframework.http.ResponseEntity<Result<Void>> handleArgument(IllegalArgumentException e) {
        Result<Void> result = Result.error(e.getMessage()); result.setCode(400);
        return org.springframework.http.ResponseEntity.badRequest().body(result);
    }
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public org.springframework.http.ResponseEntity<Result<Void>> handleStatus(org.springframework.web.server.ResponseStatusException e) {
        Result<Void> result = Result.error(e.getReason());
        result.setCode(e.getStatusCode().value());
        return org.springframework.http.ResponseEntity.status(e.getStatusCode()).body(result);
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleRuntimeException(RuntimeException e) {
        return Result.error(e.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError() == null
                ? "参数校验失败"
                : e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(message);
    }

    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String message = e.getBindingResult().getFieldError() == null
                ? "参数绑定失败"
                : e.getBindingResult().getFieldError().getDefaultMessage();
        return Result.error(message);
    }
}
