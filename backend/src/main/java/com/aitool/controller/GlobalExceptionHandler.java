package com.aitool.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.context.request.WebRequest;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException ex,
                                                                         org.springframework.http.HttpHeaders headers,
                                                                         HttpStatusCode status,
                                                                         org.springframework.web.context.request.WebRequest request) {
        log.error("文件大小超出限制: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(Map.of("message", "上传文件大小超出限制 (最大500MB)"));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatus(ResponseStatusException ex) {
        log.error("请求错误: {}", ex.getReason());
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("message", ex.getReason()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
        log.error("参数错误: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({ConnectException.class, UnknownHostException.class})
    public ResponseEntity<?> handleConnectionError(Exception ex) {
        log.error("连接错误: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("message", "无法连接到远程服务器: " + ex.getMessage()));
    }

    @ExceptionHandler(SocketTimeoutException.class)
    public ResponseEntity<?> handleTimeout(SocketTimeoutException ex) {
        log.error("连接超时: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT)
                .body(Map.of("message", "连接超时，请稍后重试"));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<?> handleIO(IOException ex) {
        log.error("IO错误: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "文件处理错误: " + ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        log.error("未处理的异常: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "服务器内部错误: " + ex.getMessage()));
    }
} 