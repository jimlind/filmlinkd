package jimlind.amaranth.exception;

// Example Log: "Exception thrown and caught in scheduled task: {}"
public class TaskGeneralException extends TaskException {
  public TaskGeneralException(String message, Throwable cause) {
    super(message, cause);
  }
}
