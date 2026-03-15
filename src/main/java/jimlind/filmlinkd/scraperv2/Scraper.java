package jimlind.filmlinkd.scraperv2;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import jimlind.filmlinkd.model.Message.PublishSource;
import jimlind.filmlinkd.scraperv1.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraperv1.cache.BaseUserCache;

/**
 * Base class for executing scraping tasks.
 *
 * <p>Manages the retrieval of user cache entries and submits scraping tasks to a virtual thread
 * executor with concurrency limits.
 */
public class Scraper {
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
   */
  Scraper(ScraperCoordinatorFactory factory, BaseUserCache userCache, PublishSource source) {
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
  public void run() {
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
