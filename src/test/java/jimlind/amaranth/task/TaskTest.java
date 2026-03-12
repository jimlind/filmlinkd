package jimlind.amaranth.task;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class TaskTest {

  private ListAppender<ILoggingEvent> listAppender;

  @BeforeEach
  void setUp() {
    Logger taskLogger = (Logger) LoggerFactory.getLogger(Task.class);
    listAppender = new ListAppender<>();
    listAppender.start();
    taskLogger.addAppender(listAppender);
  }

  @Test
  void runSafely_logsTimeout() {
    TestTask task =
        new TestTask(
            () -> {
              try {
                Thread.sleep(1000);
              } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
              }
            },
            20);

    task.runSafely();

    List<ILoggingEvent> logsList = listAppender.list;
    verifyLog(logsList, "Task timed out: TestTask");
  }

  @Test
  void runSafely_logsException() {
    RuntimeException testException = new RuntimeException("Test Exception");
    TestTask task =
        new TestTask(
            () -> {
              throw testException;
            },
            1000);

    assertDoesNotThrow(task::runSafely);

    List<ILoggingEvent> logsList = listAppender.list;
    verifyLog(logsList, "Exception thrown and caught in scheduled task: Test Exception");
  }

  @Test
  void runSafely_rethrowsError() {
    Error testError = new Error("Test Error");
    TestTask task =
        new TestTask(
            () -> {
              throw testError;
            },
            1000);

    assertThrows(Error.class, task::runSafely);

    List<ILoggingEvent> logsList = listAppender.list;
    verifyLog(
        logsList,
        "Fatal error thrown in scheduled task and task killed: java.lang.Error: Test Error");
  }

  @Test
  void runSafely_successfulExecution() {
    TestTask task = new TestTask(() -> {}, 1000);
    assertDoesNotThrow(task::runSafely);
    List<ILoggingEvent> logsList = listAppender.list;
    assert (logsList.isEmpty());
  }

  private void verifyLog(List<ILoggingEvent> logsList, String expectedMessage) {
    assert (logsList.stream()
        .anyMatch(event -> event.getFormattedMessage().contains(expectedMessage)));
  }

  private static class TestTask extends Task {
    private final Runnable taskLogic;

    public TestTask(Runnable taskLogic, long timeoutMillis) {
      this.taskLogic = taskLogic;
      this.timeoutMillis = timeoutMillis;
    }

    // Leave start empty and test runTask specifically for how throwables are processed
    @Override
    public void start() {}

    @Override
    protected void runTask() {
      taskLogic.run();
    }
  }
}
