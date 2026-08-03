package carsharing.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class LoginException extends RuntimeException {
    private final HttpStatus status;

    public LoginException(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
}
