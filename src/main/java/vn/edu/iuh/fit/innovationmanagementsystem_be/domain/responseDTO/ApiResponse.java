package vn.edu.iuh.fit.innovationmanagementsystem_be.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int statusCode;
    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, true, message, data);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, true, "Thành công", data);
    }

    public static <T> ApiResponse<T> error(String message, int statusCode) {
        return new ApiResponse<>(statusCode, false, message, null);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(400, false, message, null);
    }
}
