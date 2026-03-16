package jimlind.amaranth;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.amaranth.task.Task;

/**
 * Manages the lifecycle of multiple {@link Task} instances.
 *
 * <p>This scheduler handles starting, stopping, and supervising tasks. It includes a supervisor
 * mechanism that periodically checks if tasks have exited and restarts them to ensure continuous
 * execution.
 */
@Singleton
public class Scheduler {
  private final AtomicBoolean scheduledTasksStarted = new AtomicBoolean(false);
  private final AtomicBoolean supervisorStarted = new AtomicBoolean(false);
  private final List<Task> taskList = new ArrayList<>();
  private final ScheduledExecutorService supervisor = Executors.newSingleThreadScheduledExecutor();

  /** Constructs a new Scheduler. */
  @Inject
  public Scheduler() {}

  /**
   * Registers a task to be managed by this scheduler.
   *
   * @param task The task to add.
   */
  public void addTask(Task task) {
    taskList.add(task);
  }

  /** Starts all registered tasks and begins the supervisor thread. */
  public void startAll() {
    if (!scheduledTasksStarted.compareAndSet(false, true)) {
      throw new IllegalStateException("Scheduler can only be started once.");
    }

    taskList.forEach(Task::start);

    if (supervisorStarted.compareAndSet(false, true)) {
      supervisor.scheduleAtFixedRate(this::restartAnyExitedTasks, 1, 1, TimeUnit.SECONDS);
    }
  }

  /** Stops all registered tasks. */
  public void stopAll() {
    taskList.forEach(Task::stop);
    scheduledTasksStarted.set(false);
  }

  private void restartAnyExitedTasks() {
    for (Task task : taskList) {
      if (!task.hasExited()) {
        continue;
      }
      task.start();
    }
  }
}
