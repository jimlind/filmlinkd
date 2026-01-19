package jimlind.filmlinkd.scraper;

import java.util.concurrent.Semaphore;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.core.di.ApplicationComponent;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.scraper.cache.BaseUserCache;

/** A factory for creating instances of the {@link ScraperCoordinator} model. */
@Singleton
public class ScraperCoordinatorFactory {
  private final ApplicationComponent applicationComponent;

  @Inject
  ScraperCoordinatorFactory(ApplicationComponent applicationComponent) {
    this.applicationComponent = applicationComponent;
  }

  /**
   * Creates and initializes a new ScraperCoordinator object.
   *
   * @param semaphore Used to limit the number of concurrent tasks
   * @param userCache Where we store in memory versions records of latest diary entry
   * @param userLetterboxdId A user's Letterboxd ID
   * @param diaryEntryLetterboxdId A user's Letterboxd ID of last known diary entry
   * @param source Where the new diary entry comes from.
   * @return A new, populated {@link ScraperCoordinator} object.
   */
  public Runnable create(
      Semaphore semaphore,
      BaseUserCache userCache,
      String userLetterboxdId,
      String diaryEntryLetterboxdId,
      Message.PublishSource source) {
    ScraperCoordinator scraperCoordinator = applicationComponent.scraperCoordinator();
    scraperCoordinator.setSemaphore(semaphore);
    scraperCoordinator.setUserCache(userCache);
    scraperCoordinator.setUserLetterboxdId(userLetterboxdId);
    scraperCoordinator.setDiaryEntryLetterboxdId(diaryEntryLetterboxdId);
    scraperCoordinator.setSource(source);

    return scraperCoordinator;
  }
}
