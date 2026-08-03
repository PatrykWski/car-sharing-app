package carsharing.app.exception;

public class EmptyInventoryException extends RuntimeException {
  public EmptyInventoryException(String message) {
    super(message);
  }
}
