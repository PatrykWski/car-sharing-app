package carsharing.app.exception;

import java.time.Instant;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExceptionResponse {
    private final Instant time;
    private final String message;
    private final int status;
    private List<String> errors;

    public ExceptionResponse(Instant time, String message, int status, List<String> errors) {
        this.time = time;
        this.message = message;
        this.status = status;
        this.errors = errors;
    }

    public ExceptionResponse(Instant time, String message, int status) {
        this.time = time;
        this.message = message;
        this.status = status;
    }
}
