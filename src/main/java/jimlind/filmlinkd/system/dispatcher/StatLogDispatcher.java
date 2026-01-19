package jimlind.filmlinkd.system.dispatcher;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.core.scheduling.TimedTaskRunner;
import jimlind.filmlinkd.system.MemoryInformationLogger;

/**
 * Executes the timed stat logging tasks in a way that appropriately uses the TimedTaskRunner base
 * to ensure that things could be closed when not needed.
 */
@Singleton
public class StatLogDispatcher extends TimedTaskRunner {
  private static final long INITIAL_DELAY_MILLISECONDS = 0;
  private static final long INTERVAL_MILLISECONDS = 600000; // 10 minutes

  private final MemoryInformationLogger memoryInformationLogger;

  /**
   * Constructor for this class.
   *
   * @param memoryInformationLogger Logs information about the memory usage from the system
   */
  @Inject
  public StatLogDispatcher(MemoryInformationLogger memoryInformationLogger) {
    super(INITIAL_DELAY_MILLISECONDS, INTERVAL_MILLISECONDS);

    this.memoryInformationLogger = memoryInformationLogger;
  }

  @Override
  protected void runTask() {
    memoryInformationLogger.run();
  }
}
