package jimlind.amaranth.exception;

import java.io.Serial;

/** Indicates an interruption exception occurred during task execution. */
public class TaskInterruptionException extends TaskException {
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Constructor for this class.
   *
   * @param message Primary message used for displaying exception context
   * @param cause More complete context for the exception's creation
   */
  public TaskInterruptionException(String message, Throwable cause) {
    // Example Log: "Interrupted Exception thrown in scheduled task: {}
    super(message, cause);
  }
}
