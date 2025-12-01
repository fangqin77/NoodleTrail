package com.example.noodletrail.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Arrays;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 请求方法不支持（例如接口只支持 POST，前端却发了 GET）
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        String supported = ex.getSupportedMethods() != null ? Arrays.toString(ex.getSupportedMethods()) : "[]";
        String msg = "请求方法不支持：" + ex.getMethod() + "，支持的方法：" + supported;
        ApiResponse<Object> body = ApiResponse.fail(msg, "MethodNotAllowed");
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    /**
     * 兜底异常处理：返回异常 message，便于前端看到真实错误原因
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handle(Exception ex) {
        ex.printStackTrace();
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            message = "服务器内部错误";
        }
        ApiResponse<Object> body = ApiResponse.fail(message, "ServerError");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}