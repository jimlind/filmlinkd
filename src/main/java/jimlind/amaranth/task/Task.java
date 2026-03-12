package jimlind.amaranth.task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;

/**
 * Wrapper for a ScheduledExecutorService task.
 *
 * <p>This serves three purposes: 1) Handle timeouts on service runners and 2) ensure that
 * appropriate error and throwable events are handled correctly and 3) To ensure that the task could
 * theoretically be closed when they are not no longer needed, making static analysis tools happy
 */
@Slf4j
public abstract class Task {
  protected final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  protected final ExecutorService workerPool = Executors.newCachedThreadPool();
  protected ScheduledFuture<?> scheduledFuture;
  protected long timeoutMillis = TimeUnit.HOURS.toMillis(1);

  /**
   * Starts the ScheduledExecutorService. Needs to be implemented.
   *
   * <p>Should schedule running the `runSafely` method accordingly.
   */
  public abstract void start();

  /**
   * Returns true if any piece of the scheduling task has exited.
   *
   * @return Exit status
   */
  public boolean hasExited() {
    return scheduledFuture.isCancelled()
        || scheduledFuture.isDone()
        || scheduler.isShutdown()
        || scheduler.isTerminated();
  }

  /**
   * Stops the ScheduledExecutorService. Even though this method should not be executed in the
   * infinite use cases it satisfies static analysis tools needs by existing and good to keep around
   * if I ever want to make a task that isn't infinite.
   */
  public void stop() {
    if (scheduledFuture != null && !scheduledFuture.isCancelled()) {
      scheduledFuture.cancel(false);
    }
    scheduler.shutdown();
    workerPool.shutdown();
    try {
      if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
        scheduler.shutdownNow();
      }
      if (!workerPool.awaitTermination(1, TimeUnit.SECONDS)) {
        workerPool.shutdownNow();
      }
    } catch (InterruptedException e) {
      scheduler.shutdownNow();
      workerPool.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  /** Execution would stop if this method returned true, but it is a hardcoded infinite task */
  protected boolean shouldStop() {
    return false;
  }

  /** Task to actually run. Needs to be implemented. */
  protected abstract void runTask();

  protected void runSafely() {
    // Halt running if the scheduled task stop requested
    if (shouldStop()) {
      stop();
      return;
    }

    // Wraps the `runTask` method forcing timeouts and catching appropriate events. Throwing
    // exceptions on a task should not stop the scheduler so catch and log those. Throwing errors
    // and interruptions on a task should stop the scheduler so we need to log and rethrow those
    // events so functionality stays the same but visibility increases.
    try {
      Future<?> future = workerPool.submit(this::runTask);
      future.get(timeoutMillis, TimeUnit.MILLISECONDS);
    } catch (TimeoutException timeoutException) {
      log.error("Task timed out: {}", this.getClass().getSimpleName());
    } catch (ExecutionException executionException) {
      Throwable cause = executionException.getCause();
      if (cause instanceof Error) {
        log.error("Fatal error thrown in scheduled task and task killed: {}", cause.toString());
        throw (Error) cause;
      } else {
        log.error("Exception thrown and caught in scheduled task: {}", cause.getMessage());
      }
    } catch (InterruptedException interruption) {
      log.error("Interrupted Exception thrown in scheduled task: {}", interruption.getMessage());
      Thread.currentThread().interrupt();
      throw new RuntimeException(interruption);
    }
  }
}
