package jimlind.amaranth.exception;

// Example Log: "Interrupted Exception thrown in scheduled task: {}
public class TaskInterruptionException extends TaskException {
  public TaskInterruptionException(String message, Throwable cause) {
    super(message, cause);
  }
}
