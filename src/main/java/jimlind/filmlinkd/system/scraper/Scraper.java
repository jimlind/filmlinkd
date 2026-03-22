package jimlind.filmlinkd.system.scraper;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import jimlind.amaranth.task.FixedDelayTask;
import jimlind.filmlinkd.model.Message.PublishSource;
import jimlind.filmlinkd.scraperv1.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraperv1.cache.BaseUserCache;

/**
 * Base class for executing scraping tasks.
 *
 * <p>Manages the retrieval of user cache entries and submits scraping tasks to a virtual thread
 * executor with concurrency limits.
 */
public class Scraper extends FixedDelayTask {
  private static final long INITIAL_DELAY_MILLIS = 2000; // 2 seconds

  private final ScraperCoordinatorFactory factory;
  private final BaseUserCache userCache;
  private final PublishSource source;
  protected int concurrentClientLimit;

  /**
   * Constructs a new Scraper instance.
   *
   * @param factory Factory to create scraping tasks
   * @param userCache Cache containing user data to process
   * @param source The source type for the published messages
   * @param scraperPeriod Time allowed for a single scrape event
   */
  Scraper(
      ScraperCoordinatorFactory factory,
      BaseUserCache userCache,
      PublishSource source,
      long scraperPeriod) {
    super(INITIAL_DELAY_MILLIS, scraperPeriod, scraperPeriod);

    this.factory = factory;
    this.userCache = userCache;
    this.source = source;
  }

  /**
   * Executes the scraping process.
   *
   * <p>Iterates through the user cache and submits tasks for each entry. Concurrency is controlled
   * via a semaphore to ensure the client limit is not exceeded.
   */
  @Override
  public void runTask() {
    Semaphore semaphore = new Semaphore(concurrentClientLimit);
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (Map.Entry<String, String> entry : userCache.getNextPage()) {
        String userLid = entry.getKey();
        String diaryEntryLid = entry.getValue();

        Runnable task = factory.create(semaphore, userCache, userLid, diaryEntryLid, source);
        executor.submit(task);
      }
      executor.shutdown();
    }
  }
}
