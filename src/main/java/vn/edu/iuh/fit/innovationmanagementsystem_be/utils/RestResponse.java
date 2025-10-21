package vn.edu.iuh.fit.innovationmanagementsystem_be.utils;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestResponse<T> {
    private int statusCode;
    private String message;
    private T data;

}
