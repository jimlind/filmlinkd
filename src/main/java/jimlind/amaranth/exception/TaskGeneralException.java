package jimlind.amaranth.exception;

import java.io.Serial;

/** Indicates a lower priority general exception occurred during task execution. */
public class TaskGeneralException extends TaskException {
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Constructor for this class.
   *
   * @param message Primary message used for displaying exception context
   * @param cause More complete context for the exception's creation
   */
  public TaskGeneralException(String message, Throwable cause) {
    // Example Log: "Exception thrown and caught in scheduled task: {}"
    super(message, cause);
  }
}
