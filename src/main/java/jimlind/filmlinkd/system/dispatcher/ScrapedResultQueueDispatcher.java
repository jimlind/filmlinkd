package jimlind.filmlinkd.system.dispatcher;

import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.core.scheduling.TimedTaskRunner;
import jimlind.filmlinkd.runnable.ScrapedResultQueueChecker;
import lombok.extern.slf4j.Slf4j;

/**
 * Executes the timed scraped result queue checking tasks in a way that appropriately uses the
 * TimedTaskRunner base to ensure that things could be closed when not needed.
 */
@Singleton
@Slf4j
public class ScrapedResultQueueDispatcher extends TimedTaskRunner {
  private static final int INITIAL_DELAY_MILLISECONDS = 0;
  private static final int INTERVAL_MILLISECONDS = 100;

  private final ScrapedResultQueueChecker scrapedResultQueueChecker;

  /**
   * Constructor for this class.
   *
   * @param scrapedResultQueueChecker A class that checks the ScrapedResultQueue
   */
  @Inject
  public ScrapedResultQueueDispatcher(ScrapedResultQueueChecker scrapedResultQueueChecker) {
    super(INITIAL_DELAY_MILLISECONDS, INTERVAL_MILLISECONDS);
    this.scrapedResultQueueChecker = scrapedResultQueueChecker;
  }

  @Override
  protected void runTask() {
    scrapedResultQueueChecker.run();
  }
}
