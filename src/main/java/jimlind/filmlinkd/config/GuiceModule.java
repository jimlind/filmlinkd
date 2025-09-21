package jimlind.filmlinkd.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.cache.EntryCache;
import jimlind.filmlinkd.cache.GeneralUserCache;
import jimlind.filmlinkd.cache.VipUserCache;
import jimlind.filmlinkd.config.modules.DiscordModule;
import jimlind.filmlinkd.config.modules.GoogleModule;
import jimlind.filmlinkd.config.modules.LetterboxdModule;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.factory.ScrapedResultCheckerFactory;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.factory.VipFactory;
import jimlind.filmlinkd.reciever.CommandMessageReceiver;
import jimlind.filmlinkd.reciever.LogEntryMessageReceiver;
import jimlind.filmlinkd.runnable.GeneralScraper;
import jimlind.filmlinkd.runnable.GeneralUserCacheClearer;
import jimlind.filmlinkd.runnable.VipScraper;
import jimlind.filmlinkd.runnable.VipUserCacheClearer;
import jimlind.filmlinkd.system.MemoryInformationLogger;
import jimlind.filmlinkd.system.ScrapedResultQueue;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.scraper.GeneralScraperScheduler;
import jimlind.filmlinkd.system.scraper.VipScraperScheduler;

/** Contains all the guts for dependency injection to work. */
public class GuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    // Application Level Modules
    bind(AppConfig.class).in(Scopes.SINGLETON);
    bind(EntryCache.class).in(Scopes.SINGLETON);
    bind(ScrapedResultQueue.class).in(Scopes.SINGLETON);
    bind(ShutdownThread.class).in(Scopes.SINGLETON);

    // Factories
    bind(EmbedBuilderFactory.class).in(Scopes.SINGLETON);
    bind(ScrapedResultCheckerFactory.class).in(Scopes.SINGLETON);
    bind(UserFactory.class).in(Scopes.SINGLETON);
    bind(VipFactory.class).in(Scopes.SINGLETON);

    // Receivers
    bind(CommandMessageReceiver.class).in(Scopes.SINGLETON);
    bind(LogEntryMessageReceiver.class).in(Scopes.SINGLETON);

    // General System Modules
    bind(EntryCache.class).in(Scopes.SINGLETON);
    bind(MemoryInformationLogger.class).in(Scopes.SINGLETON);

    // Scraper Related Modules
    bind(GeneralScraperScheduler.class).in(Scopes.SINGLETON);
    bind(GeneralScraper.class).in(Scopes.SINGLETON);
    bind(GeneralUserCache.class).in(Scopes.SINGLETON);
    bind(GeneralUserCacheClearer.class).in(Scopes.SINGLETON);
    bind(VipScraperScheduler.class).in(Scopes.SINGLETON);
    bind(VipScraper.class).in(Scopes.SINGLETON);
    bind(VipUserCache.class).in(Scopes.SINGLETON);
    bind(VipUserCacheClearer.class).in(Scopes.SINGLETON);

    // Discord, Google, and Letterboxd Dependency Modules
    install(new DiscordModule());
    install(new GoogleModule());
    install(new LetterboxdModule());
  }
}
