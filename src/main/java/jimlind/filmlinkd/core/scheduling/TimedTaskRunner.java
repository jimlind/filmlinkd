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
  private final int initialDelaySeconds;
  private final int intervalSeconds;
  private ScheduledFuture<?> scheduledFuture;

  /**
   * Constructor for this class.
   *
   * @param initialDelaySeconds Number of seconds before first task is executed
   * @param intervalSeconds Number of seconds in between task execution
   */
  public TimedTaskRunner(int initialDelaySeconds, int intervalSeconds) {
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    this.initialDelaySeconds = initialDelaySeconds;
    this.intervalSeconds = intervalSeconds;
  }

  /** Task to actually run. Needs to be implemented. */
  protected abstract void runTask();

  /** Execution will stop when this method returns true. */
  protected boolean shouldStop() {
    return false;
  }

  /** Starts the SingleThreadScheduledExecutor. */
  public void start() {
    scheduledFuture =
        scheduler.scheduleAtFixedRate(
            () -> {
              runTask();
              if (shouldStop()) {
                stop();
              }
            },
            initialDelaySeconds,
            intervalSeconds,
            TimeUnit.SECONDS);
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
}
