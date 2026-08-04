package carsharing.app.exception;

public class RentalNotFinished extends RuntimeException {
  public RentalNotFinished(String message) {
    super(message);
  }
}
