package carsharing.app.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class EmptyInventoryException extends RuntimeException {
    private final HttpStatus status;

    public EmptyInventoryException(String message) {
        super(message);
        this.status = HttpStatus.NOT_FOUND;
    }
}
