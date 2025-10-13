package jimlind.filmlinkd.scraper.runner;

import com.google.inject.Inject;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.scraper.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraper.cache.BaseUserCache;
import lombok.extern.slf4j.Slf4j;

/**
 * Base scraper that gets pages of users from user cache and publishes events to notify other
 * systems.
 */
@Slf4j
public class BaseScraper implements Runnable {
  protected AppConfig appConfig;
  protected BaseUserCache userCache;
  protected ScraperCoordinatorFactory scraperCoordinatorFactory;

  protected int concurrentClientLimit;
  protected Message.PublishSource source;

  /**
   * Constructor for this class.
   *
   * @param appConfig Application configuration
   * @param userCache Where we store in memory versions records of latest diary entry
   * @param scraperCoordinatorFactory Handles the creation of ScraperCoordinator tasks
   */
  @Inject
  public BaseScraper(
      AppConfig appConfig,
      BaseUserCache userCache,
      ScraperCoordinatorFactory scraperCoordinatorFactory) {
    this.appConfig = appConfig;
    this.userCache = userCache;
    this.scraperCoordinatorFactory = scraperCoordinatorFactory;
  }

  @Override
  public void run() {
    Semaphore semaphore = new Semaphore(concurrentClientLimit);
    try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
      for (Map.Entry<String, String> entry : userCache.getNextPage()) {
        String userLid = entry.getKey();
        String diaryEntryLid = entry.getValue();

        Runnable task =
            scraperCoordinatorFactory.create(semaphore, userCache, userLid, diaryEntryLid, source);
        executor.submit(task);
      }

      executor.shutdown();
    }
  }
}
