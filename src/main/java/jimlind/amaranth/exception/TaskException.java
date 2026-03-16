package jimlind.amaranth.exception;

import java.io.Serial;

/** Parent class for all specific task exceptions. */
public class TaskException extends Exception {
  @Serial private static final long serialVersionUID = 1L;

  /**
   * Constructor for this class.
   *
   * @param message Primary message used for displaying exception context
   * @param cause More complete context for the exception's creation
   */
  public TaskException(String message, Throwable cause) {
    super(message, cause);
  }
}
