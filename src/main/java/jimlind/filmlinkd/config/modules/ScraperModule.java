package jimlind.filmlinkd.config.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.scraper.ScraperCoordinator;
import jimlind.filmlinkd.scraper.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraper.cache.GeneralUserCache;
import jimlind.filmlinkd.scraper.cache.VipUserCache;
import jimlind.filmlinkd.scraper.cache.clearer.GeneralUserCacheClearer;
import jimlind.filmlinkd.scraper.cache.clearer.VipUserCacheClearer;
import jimlind.filmlinkd.scraper.runner.GeneralScraper;
import jimlind.filmlinkd.scraper.runner.VipScraper;
import jimlind.filmlinkd.scraper.scheduler.ScraperSchedulerFactory;

/** Scraper modules for dependency injection. */
public class ScraperModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ScraperCoordinator.class).in(Scopes.NO_SCOPE);
    bind(ScraperCoordinatorFactory.class).in(Scopes.SINGLETON);
    bind(ScraperSchedulerFactory.class).in(Scopes.SINGLETON);

    bind(GeneralScraper.class).in(Scopes.SINGLETON);
    bind(GeneralUserCache.class).in(Scopes.SINGLETON);
    bind(GeneralUserCacheClearer.class).in(Scopes.SINGLETON);

    bind(VipScraper.class).in(Scopes.SINGLETON);
    bind(VipUserCache.class).in(Scopes.SINGLETON);
    bind(VipUserCacheClearer.class).in(Scopes.SINGLETON);
  }
}
