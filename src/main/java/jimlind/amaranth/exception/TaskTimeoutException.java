package jimlind.amaranth.exception;

public class TaskTimeoutException extends TaskException {
  public TaskTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
