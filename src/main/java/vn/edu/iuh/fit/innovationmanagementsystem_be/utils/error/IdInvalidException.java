package vn.edu.iuh.fit.innovationmanagementsystem_be.utils.error;

public class IdInvalidException extends RuntimeException {
    public IdInvalidException(String message) {
        super(message);
    }

    public IdInvalidException(String message, Throwable cause) {
        super(message, cause);
    }
}