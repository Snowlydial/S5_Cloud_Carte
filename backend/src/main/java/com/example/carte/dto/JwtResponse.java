package com.example.carte.dto;

public class JwtResponse<T> {

    private String status;
    private T data;
    private String error;

    public JwtResponse(String status, T data, String error) {
        this.status = status;
        this.data = data;
        this.error = error;
    }

    public static <T> JwtResponse<T> success(T data) {
        return new JwtResponse<>("success", data, null);
    }

    public static <T> JwtResponse<T> error(String msg) {
        return new JwtResponse<>("error", null, msg);
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
