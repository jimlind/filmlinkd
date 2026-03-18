package jimlind.amaranth.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.concurrent.atomic.AtomicReference;
import jimlind.amaranth.exception.TaskException;
import jimlind.amaranth.exception.TaskGeneralException;
import jimlind.amaranth.exception.TaskSeriousException;
import jimlind.amaranth.exception.TaskTimeoutException;
import org.junit.jupiter.api.Test;

class TaskTest {

  @Test
  void runSafely_capturesTimeout() {
    AtomicReference<TaskException> captured = new AtomicReference<>();
    TestTask task =
        new TestTask(
            () -> {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            },
            20,
            captured);
    task.runSafely();
    assertInstanceOf(TaskTimeoutException.class, captured.get());
  }

  @Test
  void runSafely_capturesException() {
    AtomicReference<TaskException> captured = new AtomicReference<>();
    RuntimeException testException = new RuntimeException("Test Exception");
    TestTask task =
        new TestTask(
            () -> {
              throw testException;
            },
            1000,
            captured);
    assertDoesNotThrow(task::runSafely);
    assertInstanceOf(TaskGeneralException.class, captured.get());
  }

  @Test
  void runSafely_rethrowsAndCapturesError() {
    AtomicReference<TaskException> captured = new AtomicReference<>();
    Error testError = new Error("Test Error");
    TestTask task =
        new TestTask(
            () -> {
              throw testError;
            },
            1000,
            captured);
    assertThrows(Error.class, task::runSafely);
    assertInstanceOf(TaskSeriousException.class, captured.get());
  }

  @Test
  void runSafely_successfulExecution() {
    AtomicReference<TaskException> captured = new AtomicReference<>();
    TestTask task = new TestTask(() -> {}, 1000, captured);
    assertDoesNotThrow(task::runSafely);
    assertNull(captured.get());
  }

  private static class TestTask extends Task {
    private final Runnable taskLogic;
    private final AtomicReference<TaskException> exceptionCaptor;

    public TestTask(
        Runnable taskLogic, long timeoutMillis, AtomicReference<TaskException> exceptionCaptor) {
      this.taskLogic = taskLogic;
      this.timeoutMillis = timeoutMillis;
      this.exceptionCaptor = exceptionCaptor;
    }

    // Leave start empty and test runTask specifically for how exceptions are processed
    @Override
    public void start() {}

    @Override
    protected void runTask() {
      taskLogic.run();
    }

    @Override
    protected void exceptionConsumer(TaskException exception) {
      exceptionCaptor.set(exception);
    }
  }
}
