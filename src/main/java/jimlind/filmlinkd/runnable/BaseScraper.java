package jimlind.filmlinkd.runnable;

import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import jimlind.filmlinkd.cache.BaseUserCache;
import jimlind.filmlinkd.factory.ScraperCoordinatorFactory;
import jimlind.filmlinkd.model.Message;
import lombok.extern.slf4j.Slf4j;

/**
 * Base scraper that gets pages of users from user cache and publishes events to notify other
 * systems.
 */
@Slf4j
public class BaseScraper implements Runnable {
  protected BaseUserCache userCache;
  protected ScraperCoordinatorFactory scraperCoordinatorFactory;

  protected Message.PublishSource source;

  /**
   * Constructor for this class.
   *
   * @param userCache Where we store in memory versions records of latest diary entry
   * @param scraperCoordinatorFactory Handles the creation of ScraperCoordinator tasks
   */
  @Inject
  public BaseScraper(BaseUserCache userCache, ScraperCoordinatorFactory scraperCoordinatorFactory) {
    this.userCache = userCache;
    this.scraperCoordinatorFactory = scraperCoordinatorFactory;
  }

  @Override
  public void run() {
    List<Map.Entry<String, String>> userPage = userCache.getNextPage();
    for (Map.Entry<String, String> entry : userPage) {
      String userLetterboxdId = entry.getKey();
      String diaryEntryLetterboxdId = entry.getValue();

      Runnable task =
          scraperCoordinatorFactory.create(
              userCache, userLetterboxdId, diaryEntryLetterboxdId, source);
      Thread.startVirtualThread(task);
    }
  }
}
