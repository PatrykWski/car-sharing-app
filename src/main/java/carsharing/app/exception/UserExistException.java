package carsharing.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class UserExistException extends RuntimeException {
    private final HttpStatus status;

    public UserExistException(String message) {
        super(message);
        this.status = HttpStatus.CONFLICT;
    }
}
