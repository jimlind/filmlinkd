package jimlind.amaranth.task;

import java.util.concurrent.TimeUnit;

/** Abstraction that wraps a ScheduledExecutorService using scheduleAtFixedRate. */
public abstract class FixedRateTask extends Task {
  private final long initialDelayMillis;
  private final long periodMillis;

  /**
   * Constructor for this class.
   *
   * @param initialDelayMillis Number of milliseconds before first task is executed
   * @param periodMillis Number of milliseconds for a task period
   * @param timeoutMillis Number of milliseconds to allow a task execution to run
   */
  public FixedRateTask(long initialDelayMillis, long periodMillis, long timeoutMillis) {
    this.initialDelayMillis = initialDelayMillis;
    this.periodMillis = periodMillis;
    this.timeoutMillis = timeoutMillis;
  }

  /** Task to actually run. Needs to be implemented. */
  @Override
  protected abstract void runTask();

  /** Starts the SingleThreadScheduledExecutor using scheduleAtFixedRate. */
  @Override
  public void start() {
    if (periodMillis <= 0) {
      throw new IllegalArgumentException("Period must be positive: " + periodMillis);
    }

    scheduledFuture =
        scheduler.scheduleAtFixedRate(
            this::runSafely, initialDelayMillis, periodMillis, TimeUnit.MILLISECONDS);
  }
}
