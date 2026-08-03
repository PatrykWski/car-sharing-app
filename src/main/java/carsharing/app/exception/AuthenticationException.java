package carsharing.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthenticationException extends RuntimeException {
    private final HttpStatus status;

    public AuthenticationException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
}
