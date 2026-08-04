package carsharing.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class NotificationError extends RuntimeException {
    private HttpStatus status;

    public NotificationError(String message) {
        super(message);
        this.status = HttpStatus.BAD_REQUEST;
    }
}
