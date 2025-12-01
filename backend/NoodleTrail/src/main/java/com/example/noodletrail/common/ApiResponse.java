package com.example.noodletrail.common;

public record ApiResponse<T>(boolean success, T data, String message, String code) {

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, null);
    }

    public static ApiResponse<Object> fail(String message, String code) {
        return new ApiResponse<>(false, null, message, code);
    }
}