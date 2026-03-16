package jimlind.amaranth.task;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import jimlind.amaranth.exception.TaskException;
import jimlind.amaranth.exception.TaskGeneralException;
import jimlind.amaranth.exception.TaskInterruptionException;
import jimlind.amaranth.exception.TaskSeriousException;
import jimlind.amaranth.exception.TaskTimeoutException;

/**
 * Wrapper for a ScheduledExecutorService task.
 *
 * <p>This serves three purposes: 1) Handle timeouts on service runners and 2) ensure that
 * appropriate error and throwable events are handled correctly and 3) To ensure that the task could
 * theoretically be closed when they are not no longer needed, making static analysis tools happy
 */
public abstract class Task {
  protected final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
  protected final ExecutorService workerPool = Executors.newCachedThreadPool();
  protected Consumer<TaskException> exceptionConsumer = exception -> {};
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

  /** Execution would stop if this method returned true, but it is a hardcoded infinite task. */
  protected boolean shouldStop() {
    return false;
  }

  /** Task to actually run. Needs to be implemented. */
  protected abstract void runTask();

  /** Executes the task logic safely, enforcing timeouts and handling exceptions. */
  @SuppressWarnings({"PMD.AvoidCatchingGenericException", "PMD.PreserveStackTrace"})
  protected void runSafely() {
    // Halt running if the scheduled task stop requested
    if (shouldStop()) {
      stop();
      return;
    }

    // Wraps the `runTask` method forcing timeouts and catching appropriate events.
    // All exceptions are translated and sent to the exceptionConfuser for logging.
    // Serious exceptions are rethrown to appropriately bubble up.
    //
    // * Any TimeoutException are swallowed and sent to the exceptionConsumer as
    //   TaskTimeoutException.
    // * Any InterruptedException are swallowed, sent to the exceptionConsumer as
    //   TaskInterruptionException, and the interrupt flag is restored.
    // * Any ExecutionException caused by Errors are rethrown but sent to the exceptionConsumer as
    //   TaskSeriousException first.
    // * All other ExecutionException are swallowed and send to the exceptionConsumer as
    //   TaskGeneralException.
    Future<?> future = workerPool.submit(this::runTask);
    try {
      future.get(timeoutMillis, TimeUnit.MILLISECONDS);

    } catch (TimeoutException timeout) {
      future.cancel(true);
      exceptionConsumer.accept(new TaskTimeoutException(timeout.getMessage(), timeout));

    } catch (InterruptedException interrupt) {
      future.cancel(true);
      exceptionConsumer.accept(new TaskInterruptionException(interrupt.getMessage(), interrupt));
      Thread.currentThread().interrupt();

    } catch (ExecutionException executionException) {
      Throwable cause = executionException.getCause();
      if (cause instanceof Error) {
        exceptionConsumer.accept(new TaskSeriousException(executionException.getMessage(), cause));
        throw (Error) cause;
      } else {
        exceptionConsumer.accept(new TaskGeneralException(executionException.getMessage(), cause));
      }

    } catch (Exception exception) {
      future.cancel(true);
      exceptionConsumer.accept(new TaskGeneralException(exception.getMessage(), exception));
    }
  }
}
