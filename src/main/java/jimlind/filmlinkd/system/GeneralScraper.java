package jimlind.filmlinkd.system;

import com.google.inject.Inject;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.runnable.GeneralScraperTask;
import jimlind.filmlinkd.runnable.GeneralUserCacheClearTask;

public class GeneralScraper {
  private final AppConfig appConfig;
  private final GeneralScraperTask generalScraperTask;
  private final GeneralUserCache generalUserCache;
  private final GeneralUserCacheClearTask generalUserCacheClearTask;

  @Inject
  public GeneralScraper(
      AppConfig appConfig,
      GeneralScraperTask generalScraperTask,
      GeneralUserCache generalUserCache,
      GeneralUserCacheClearTask generalUserCacheClearTask) {
    this.appConfig = appConfig;
    this.generalScraperTask = generalScraperTask;
    this.generalUserCache = generalUserCache;
    this.generalUserCacheClearTask = generalUserCacheClearTask;
  }

  public void start() {
    generalUserCache.initializeRandomPage();

    ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(
        generalScraperTask, 0, appConfig.getScraperGeneralPeriod(), TimeUnit.SECONDS);
    scheduler.scheduleAtFixedRate(
        generalUserCacheClearTask, 0, appConfig.getScraperGeneralPeriod(), TimeUnit.HOURS);
  }
}
