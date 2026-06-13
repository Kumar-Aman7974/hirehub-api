package com.hirehub.hirehubapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {

    // for return response
    private boolean success;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // static method
    public static <T> ApiResponse<T> success(String message, T data) {

        return new ApiResponse<>(true, message, data);
    }

    public  static <T> ApiResponse<T> error(String message){

        return new ApiResponse<>(false, message, null);

    }
}
