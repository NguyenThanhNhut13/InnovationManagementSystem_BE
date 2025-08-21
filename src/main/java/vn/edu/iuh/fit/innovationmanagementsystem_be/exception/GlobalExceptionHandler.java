package vn.edu.iuh.fit.innovationmanagementsystem_be.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import vn.edu.iuh.fit.innovationmanagementsystem_be.utils.RestResponse;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 0. Bắt lỗi liên quan đến ID không hợp lệ hoặc không tìm thấy người dùng
    @ExceptionHandler(value = {
            UsernameNotFoundException.class,
            IdInvalidException.class,
    })
    public ResponseEntity<RestResponse<Object>> handleIdException(Exception ex) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getMessage());
        res.setMessage("Có lỗi xảy ra !");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    // 1. Bắt lỗi validation @NotBlank, @NotNull, @Email, @Min...
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RestResponse<Object>> handleValidationErrors(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        RestResponse<Object> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setError(ex.getBody().getDetail());
        response.setMessage(String.join(", ", errors));

        return ResponseEntity.badRequest().body(response);
    }

    // 2. Bắt lỗi không tìm thấy tài nguyên
    @ExceptionHandler(value = { NoResourceFoundException.class })
    public ResponseEntity<RestResponse<Object>> handleNotFoundException(Exception ex) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.NOT_FOUND.value());
        res.setError(ex.getMessage());
        res.setMessage("404 Not Found. URL có thể không tồn tại !");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }

    // 3. Bắt các lỗi chung khác
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<RestResponse<Object>> handleRuntimeException(RuntimeException ex) {
        RestResponse<Object> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.setError("Internal Server Error");
        response.setMessage(ex.getMessage());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    // 4. Bắt lỗi khi dùng sai HTTP method (405 Method Not Allowed)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<RestResponse<Object>> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        RestResponse<Object> response = new RestResponse<>();
        response.setStatusCode(HttpStatus.METHOD_NOT_ALLOWED.value());
        response.setError("Method Not Allowed");
        response.setMessage(
                String.format("Method %s không được hỗ trợ cho endpoint này. Method được hỗ trợ là %s",
                        ex.getMethod(), ex.getSupportedHttpMethods()));

        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(response);
    }

    // 5. Bắt lỗi authentication
    @ExceptionHandler(value = {
            BadCredentialsException.class,
            AuthenticationException.class,
    })
    public ResponseEntity<RestResponse<Object>> handleAuthenticationException(Exception ex) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setError("UNAUTHORIZED");
        res.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }
}
