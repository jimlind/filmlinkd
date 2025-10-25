package jimlind.filmlinkd.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.cache.EntryCache;
import jimlind.filmlinkd.config.modules.DiscordModule;
import jimlind.filmlinkd.config.modules.GoogleModule;
import jimlind.filmlinkd.config.modules.LetterboxdModule;
import jimlind.filmlinkd.factory.CommandFactory;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.factory.MessageFactory;
import jimlind.filmlinkd.factory.ScrapedResultFactory;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.factory.VipFactory;
import jimlind.filmlinkd.reciever.CommandMessageReceiver;
import jimlind.filmlinkd.reciever.LogEntryMessageReceiver;
import jimlind.filmlinkd.runnable.ScrapedResultQueueChecker;
import jimlind.filmlinkd.scraper.ScraperCoordinator;
import jimlind.filmlinkd.scraper.ScraperCoordinatorFactory;
import jimlind.filmlinkd.scraper.cache.GeneralUserCache;
import jimlind.filmlinkd.scraper.cache.VipUserCache;
import jimlind.filmlinkd.scraper.cache.clearer.GeneralUserCacheClearer;
import jimlind.filmlinkd.scraper.cache.clearer.VipUserCacheClearer;
import jimlind.filmlinkd.scraper.runner.GeneralScraper;
import jimlind.filmlinkd.scraper.runner.VipScraper;
import jimlind.filmlinkd.scraper.scheduler.ScraperSchedulerFactory;
import jimlind.filmlinkd.system.MemoryInformationLogger;
import jimlind.filmlinkd.system.ScrapedResultQueue;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.UserCoordinator;
import jimlind.filmlinkd.system.dispatcher.ScrapedResultQueueDispatcher;
import jimlind.filmlinkd.system.dispatcher.StatLogDispatcher;

/** Contains all the guts for dependency injection to work. */
public class GuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    binder().requireExplicitBindings();

    // Application Level Modules
    bind(AppConfig.class).in(Scopes.SINGLETON);
    bind(EntryCache.class).in(Scopes.SINGLETON);
    bind(ScrapedResultQueue.class).in(Scopes.SINGLETON);
    bind(ShutdownThread.class).in(Scopes.SINGLETON);

    // Factories
    bind(CommandFactory.class).in(Scopes.SINGLETON);
    bind(EmbedBuilderFactory.class).in(Scopes.SINGLETON);
    bind(MessageFactory.class).in(Scopes.SINGLETON);
    bind(ScrapedResultFactory.class).in(Scopes.SINGLETON);
    bind(UserFactory.class).in(Scopes.SINGLETON);
    bind(VipFactory.class).in(Scopes.SINGLETON);

    // Receivers
    bind(CommandMessageReceiver.class).in(Scopes.SINGLETON);
    bind(LogEntryMessageReceiver.class).in(Scopes.SINGLETON);

    // Runnable
    bind(ScrapedResultQueueChecker.class).in(Scopes.SINGLETON);

    // General System Modules
    bind(EntryCache.class).in(Scopes.SINGLETON);
    bind(MemoryInformationLogger.class).in(Scopes.SINGLETON);
    bind(UserCoordinator.class).in(Scopes.SINGLETON);

    // System Dispatcher Modules
    bind(ScrapedResultQueueDispatcher.class).in(Scopes.SINGLETON);
    bind(StatLogDispatcher.class).in(Scopes.SINGLETON);

    // Scraper Related Modules
    bind(ScraperSchedulerFactory.class).in(Scopes.SINGLETON);
    bind(GeneralScraper.class).in(Scopes.SINGLETON);
    bind(GeneralUserCache.class).in(Scopes.SINGLETON);
    bind(GeneralUserCacheClearer.class).in(Scopes.SINGLETON);
    bind(ScraperCoordinator.class).in(Scopes.NO_SCOPE);
    bind(ScraperCoordinatorFactory.class).in(Scopes.SINGLETON);
    bind(VipScraper.class).in(Scopes.SINGLETON);
    bind(VipUserCache.class).in(Scopes.SINGLETON);
    bind(VipUserCacheClearer.class).in(Scopes.SINGLETON);

    // Discord, Google, and Letterboxd Dependency Modules
    install(new DiscordModule());
    install(new GoogleModule());
    install(new LetterboxdModule());
  }
}
