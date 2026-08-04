package carsharing.app.exception;

public class StripeProcessinException extends RuntimeException {
  public StripeProcessinException(String message) {
    super(message);
  }
}
