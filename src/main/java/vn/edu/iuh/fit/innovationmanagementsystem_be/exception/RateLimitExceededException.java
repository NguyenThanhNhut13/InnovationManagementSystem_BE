package vn.edu.iuh.fit.innovationmanagementsystem_be.exception;

public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException(String message) {
        super(message);
    }
}
