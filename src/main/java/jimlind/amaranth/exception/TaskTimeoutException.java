package jimlind.amaranth.exception;

import java.io.Serial;

/** Indicates a timeout exception occurred during task execution. */
public class TaskTimeoutException extends TaskException {
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Constructor for this class.
   *
   * @param message Primary message used for displaying exception context
   * @param cause More complete context for the exception's creation
   */
  public TaskTimeoutException(String message, Throwable cause) {
    super(message, cause);
  }
}
