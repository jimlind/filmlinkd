package jimlind.filmlinkd.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.cache.EntryCache;
import jimlind.filmlinkd.cache.GeneralUserCache;
import jimlind.filmlinkd.cache.VipUserCache;
import jimlind.filmlinkd.config.modules.DiscordModule;
import jimlind.filmlinkd.config.modules.LetterboxdModule;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.factory.ScrapedResultCheckerFactory;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.reciever.CommandMessageReceiver;
import jimlind.filmlinkd.reciever.LogEntryMessageReceiver;
import jimlind.filmlinkd.runnable.GeneralScraper;
import jimlind.filmlinkd.runnable.GeneralUserCacheClearer;
import jimlind.filmlinkd.runnable.StatLogger;
import jimlind.filmlinkd.runnable.VipScraper;
import jimlind.filmlinkd.runnable.VipUserCacheClearer;
import jimlind.filmlinkd.system.ScrapedResultQueue;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.google.PubSubSubscriberListener;
import jimlind.filmlinkd.system.google.SecretManager;
import jimlind.filmlinkd.system.google.firestore.FirestoreProvider;
import jimlind.filmlinkd.system.google.firestore.UserReader;
import jimlind.filmlinkd.system.google.firestore.UserWriter;
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

    // Receivers
    bind(CommandMessageReceiver.class).in(Scopes.SINGLETON);
    bind(LogEntryMessageReceiver.class).in(Scopes.SINGLETON);

    // General System Modules
    bind(EntryCache.class).in(Scopes.SINGLETON);
    bind(PubSubSubscriberListener.class).in(Scopes.SINGLETON);
    bind(StatLogger.class).in(Scopes.SINGLETON);

    // Scraper Related Modules
    bind(GeneralScraperScheduler.class).in(Scopes.SINGLETON);
    bind(GeneralScraper.class).in(Scopes.SINGLETON);
    bind(GeneralUserCache.class).in(Scopes.SINGLETON);
    bind(GeneralUserCacheClearer.class).in(Scopes.SINGLETON);
    bind(VipScraperScheduler.class).in(Scopes.SINGLETON);
    bind(VipScraper.class).in(Scopes.SINGLETON);
    bind(VipUserCache.class).in(Scopes.SINGLETON);
    bind(VipUserCacheClearer.class).in(Scopes.SINGLETON);

    // Google System Modules
    bind(SecretManager.class).in(Scopes.SINGLETON);
    bind(FirestoreProvider.class).in(Scopes.SINGLETON);
    bind(UserReader.class).in(Scopes.SINGLETON);
    bind(UserWriter.class).in(Scopes.SINGLETON);

    // Discord and Letterboxd Dependency Modules
    install(new DiscordModule());
    install(new LetterboxdModule());
  }
}
