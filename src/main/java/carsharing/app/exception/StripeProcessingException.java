package carsharing.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class StripeProcessingException extends RuntimeException {
    private final HttpStatus status;

    public StripeProcessingException(String message, Throwable cause) {
        super(message, cause);
        this.status = HttpStatus.BAD_REQUEST;
    }

    public StripeProcessingException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
}
