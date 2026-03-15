package jimlind.filmlinkd.scraperV2;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import jimlind.filmlinkd.model.Message.PublishSource;
import jimlind.filmlinkd.scraperV1.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraperV1.cache.BaseUserCache;

public class Scraper {
  private final ScraperCoordinatorFactory factory;
  private final BaseUserCache userCache;
  private final PublishSource source;
  protected int concurrentClientLimit;

  Scraper(ScraperCoordinatorFactory factory, BaseUserCache userCache, PublishSource source) {
    this.factory = factory;
    this.userCache = userCache;
    this.source = source;
  }

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
