package jimlind.filmlinkd.scraper.scheduler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.runnable.BaseScraper;
import jimlind.filmlinkd.runnable.BaseUserCacheClearer;
import jimlind.filmlinkd.scraper.cache.BaseUserCache;

/** Schedules the scheduler that runs over every user. */
public class ScraperScheduler {
  protected BaseScraper scraper;
  protected BaseUserCache userCache;
  protected BaseUserCacheClearer userCacheClearer;
  protected long scraperPeriod;
  protected long userCachePeriod;

  /**
   * Constructor for this class.
   *
   * @param scraper The scraper task to schedule
   * @param userCache In memory cache of latest diary entry
   * @param userCacheClearer The cache clearing task to schedule
   * @param scraperPeriod Delay between scraping events in seconds
   * @param userCachePeriod Delay between cache clearing events in hours
   */
  public ScraperScheduler(
      BaseScraper scraper,
      BaseUserCache userCache,
      BaseUserCacheClearer userCacheClearer,
      long scraperPeriod,
      long userCachePeriod) {
    this.scraper = scraper;
    this.userCache = userCache;
    this.userCacheClearer = userCacheClearer;
    this.scraperPeriod = scraperPeriod;
    this.userCachePeriod = userCachePeriod;
  }

  /** Method that activates the scraper and clearer. */
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
