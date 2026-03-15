package jimlind.amaranth.exception;

// Example Log: Fatal error thrown in scheduled task and task killed: {}", cause.toString());
public class TaskSeriousException extends TaskException {
  public TaskSeriousException(String message, Throwable cause) {
    super(message, cause);
  }
}
