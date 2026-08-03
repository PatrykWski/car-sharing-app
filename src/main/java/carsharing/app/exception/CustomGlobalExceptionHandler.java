package carsharing.app.exception;

import java.time.Instant;
import java.util.ArrayList;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class CustomGlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        ExceptionResponse exceptionResponse = new ExceptionResponse(
                Instant.now(),
                "Validation failed",
                status.value(),
                new ArrayList<>(ex.getBindingResult().getAllErrors().stream()
                        .map(ObjectError::getDefaultMessage).toList()));

        return new ResponseEntity<>(exceptionResponse, status);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllUncaughtExceptions(Exception ex) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                Instant.now(),
                "An unexpected error occurred: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR.value()
        );
        return new ResponseEntity<>(exceptionResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Object> handleEntityNotFoundException(EntityNotFoundException ex) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                Instant.now(),
                ex.getMessage(),
                ex.getStatus().value()
        );
        return new ResponseEntity<>(exceptionResponse, ex.getStatus());
    }

    @ExceptionHandler(LoginException.class)
    public ResponseEntity<Object> handleLoginException(LoginException ex) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                Instant.now(),
                ex.getMessage(),
                ex.getStatus().value()
        );
        return new ResponseEntity<>(exceptionResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(UserExistException.class)
    public ResponseEntity<Object> handleUserExistException(UserExistException ex) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(
                Instant.now(),
                ex.getMessage(),
                ex.getStatus().value()
        );
        return new ResponseEntity<>(exceptionResponse, HttpStatus.CONFLICT);
    }
}
