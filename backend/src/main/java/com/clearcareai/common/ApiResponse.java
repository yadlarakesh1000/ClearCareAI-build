package com.clearcareai.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // shortcut for a successful response
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true).message(message).data(data).build();
    }

    // shortcut for a failed response (no data to send back)
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false).message(message).build();
    }
}
