package jimlind.filmlinkd.factory;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.concurrent.Semaphore;
import jimlind.filmlinkd.cache.BaseUserCache;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.runnable.ScraperCoordinator;

/** A factory for creating instances of the {@link ScraperCoordinator} model. */
public class ScraperCoordinatorFactory {
  private final Injector injector;

  @Inject
  ScraperCoordinatorFactory(Injector injector) {
    this.injector = injector;
  }

  /**
   * Creates and initializes a new ScraperCoordinator object.
   *
   * @param semaphore Used to limit the number of concurrent tasks
   * @param userCache Where we store in memory versions records of latest diary entry
   * @param userLetterboxdId A user's Letterboxd ID
   * @param diaryEntryLetterboxdId A user's Letterboxd ID of last known diary entry
   * @param source Where the new diary entry comes from.
   * @param scrapeEntryWithRss Indicate if we should use RSS or API instead
   * @return A new, populated {@link ScraperCoordinator} object.
   */
  public Runnable create(
      Semaphore semaphore,
      BaseUserCache userCache,
      String userLetterboxdId,
      String diaryEntryLetterboxdId,
      Message.PublishSource source,
      boolean scrapeEntryWithRss) {
    ScraperCoordinator scraperCoordinator = injector.getInstance(ScraperCoordinator.class);
    scraperCoordinator.setSemaphore(semaphore);
    scraperCoordinator.setUserCache(userCache);
    scraperCoordinator.setUserLetterboxdId(userLetterboxdId);
    scraperCoordinator.setDiaryEntryLetterboxdId(diaryEntryLetterboxdId);
    scraperCoordinator.setSource(source);
    scraperCoordinator.setScrapeEntryWithRss(scrapeEntryWithRss);

    return scraperCoordinator;
  }
}
