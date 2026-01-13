package com.example.carte.dto;

public class ApiResponse<T> {

    private String status;
    private T data;
    private String error;

    public ApiResponse(String status, T data, String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", data, null);
    }

    public static <T> ApiResponse<T> error(String msg) {
        return new ApiResponse<>("error", null, msg);
    }

    public String getStatus() {
        return status;
    }

    public T getData() {
        return data;
    }

    public String getError() {
        return error;
    }
}
