package vn.edu.iuh.fit.innovationmanagementsystem_be.exception;

/**
 * Exception được sử dụng khi không tìm thấy resource
 * Trả về HTTP status code 404 (Not Found)
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
