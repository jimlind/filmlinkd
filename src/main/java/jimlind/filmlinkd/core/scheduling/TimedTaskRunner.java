package jimlind.filmlinkd.core.scheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstraction that wraps a SingleThreadScheduledExecutor to ensure that things are closed when not
 * needed.
 */
@Slf4j
public abstract class TimedTaskRunner {
  private final ScheduledExecutorService scheduler;
  private final long initialDelayMilliseconds;
  private final long intervalMilliseconds;
  private ScheduledFuture<?> scheduledFuture;

  /**
   * Constructor for this class.
   *
   * @param initialDelayMilliseconds Number of milliseconds before first task is executed
   * @param intervalMilliseconds Number of milliseconds in between task execution
   */
  public TimedTaskRunner(long initialDelayMilliseconds, long intervalMilliseconds) {
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    this.initialDelayMilliseconds = initialDelayMilliseconds;
    this.intervalMilliseconds = intervalMilliseconds;
  }

  /** Task to actually run. Needs to be implemented. */
  protected abstract void runTask();

  /** Execution will stop when this method returns true. */
  protected boolean shouldStop() {
    return false;
  }

  /** Starts the SingleThreadScheduledExecutor. */
  public void start() {
    if (intervalMilliseconds <= 0) {
      throw new IllegalArgumentException("Interval must be positive: " + intervalMilliseconds);
    }

    scheduledFuture =
        scheduler.scheduleAtFixedRate(
            this::runSafely, initialDelayMilliseconds, intervalMilliseconds, TimeUnit.MILLISECONDS);
  }

  /** Stops the SingleThreadScheduledExecutor. */
  public void stop() {
    if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
      scheduledFuture.cancel(false);
    }
    scheduler.shutdown();
    try {
      if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  /** Runs the task in a try stop and attempts to trigger stopping appropriately. */
  private void runSafely() {
    try {
      runTask();
    } catch (Throwable t) {
      log.error("Error running task", t);
    }
    if (shouldStop()) {
      stop();
    }
  }
}
