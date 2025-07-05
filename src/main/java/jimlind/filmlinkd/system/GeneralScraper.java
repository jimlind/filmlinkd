package jimlind.filmlinkd.system;

import com.google.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.runnable.GeneralScraperTask;

public class GeneralScraper {
  private final AppConfig appConfig;
  private final GeneralScraperTask generalScraperTask;
  private final UserCache userCache;

  @Inject
  public GeneralScraper(
      AppConfig appConfig, GeneralScraperTask generalScraperTask, UserCache userCache) {
    this.appConfig = appConfig;
    this.generalScraperTask = generalScraperTask;
    this.userCache = userCache;
  }

  public void start() {
    userCache.initializeRandomPage();

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(
        generalScraperTask, 0, appConfig.getScraperGeneralPeriod(), TimeUnit.SECONDS);
    // TODO: Schedule User Cache Clearing
  }
}
