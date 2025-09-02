package jimlind.filmlinkd.system.scraper;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.cache.BaseUserCache;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.runnable.BaseScraper;
import jimlind.filmlinkd.runnable.BaseUserCacheClearer;

/** Constructor for this class. */
public class BaseScraperScheduler {
  protected AppConfig appConfig;
  protected BaseScraper scraper;
  protected BaseUserCache userCache;
  protected BaseUserCacheClearer userCacheClearer;
  protected long scraperPeriod;
  protected long userCachePeriod;

  /** Method that activates the scrapers so that the Class can be instantiated but inactive. */
  public void start() {
    // Set up the user cache with an appropriate random starting point before the scrapers start
    userCache.initializeRandomPage();

    // These should run forever so not closing them
    @SuppressWarnings("PMD.CloseResource")
    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleWithFixedDelay(scraper, 0, scraperPeriod, TimeUnit.SECONDS);
    scheduler.scheduleWithFixedDelay(
        userCacheClearer, userCachePeriod, userCachePeriod, TimeUnit.HOURS);
  }
}
