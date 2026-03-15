package jimlind.amaranth.task;

import java.util.concurrent.TimeUnit;

/** Abstraction that wraps a ScheduledExecutorService using scheduleWithFixedDelay */
public abstract class FixedDelayTask extends Task {
  private final long initialDelayMillis;
  private final long subsequentDelayMillis;

  /**
   * Constructor for this class.
   *
   * @param initialDelayMillis Number of milliseconds before first task is executed
   * @param subsequentDelayMillis Number of milliseconds in between task execution
   * @param timeoutMillis Number of milliseconds to allow a task execution to run
   */
  public FixedDelayTask(long initialDelayMillis, long subsequentDelayMillis, long timeoutMillis) {
    this.initialDelayMillis = initialDelayMillis;
    this.subsequentDelayMillis = subsequentDelayMillis;
    this.timeoutMillis = timeoutMillis;
  }

  /** Task to actually run. Needs to be implemented. */
  protected abstract void runTask();

  /** Starts the SingleThreadScheduledExecutor using scheduleWithFixedDelay. */
  @Override
  public void start() {
    if (subsequentDelayMillis <= 0) {
      throw new IllegalArgumentException("Interval must be positive: " + subsequentDelayMillis);
    }

    scheduledFuture =
        scheduler.scheduleWithFixedDelay(
            this::runSafely, initialDelayMillis, subsequentDelayMillis, TimeUnit.MILLISECONDS);
  }
}
