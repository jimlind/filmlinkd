package jimlind.filmlinkd.core.scheduling;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class TimedTaskRunner {
  private final ScheduledExecutorService scheduler;
  private final int initialDelaySeconds;
  private final int intervalSeconds;
  private ScheduledFuture<?> scheduledFuture;

  public TimedTaskRunner(int initialDelaySeconds, int intervalSeconds) {
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    this.initialDelaySeconds = initialDelaySeconds;
    this.intervalSeconds = intervalSeconds;
  }

  protected abstract void runTask();

  protected boolean shouldStop() {
    return false;
  }

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
