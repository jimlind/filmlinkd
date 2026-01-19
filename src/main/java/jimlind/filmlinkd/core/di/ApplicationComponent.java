package jimlind.filmlinkd.core.di;

import dagger.Component;
import java.util.Map;
import javax.inject.Provider;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.discord.dispatcher.HelpEmbedDispatcher;
import jimlind.filmlinkd.discord.event.handler.Handler;
import jimlind.filmlinkd.google.pubsub.PubSubManager;
import jimlind.filmlinkd.scraper.ScraperCoordinator;
import jimlind.filmlinkd.scraper.scheduler.ScraperSchedulerFactory;
import jimlind.filmlinkd.system.DiscordSystem;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.dispatcher.ScrapedResultQueueDispatcher;
import jimlind.filmlinkd.system.dispatcher.StatLogDispatcher;

/** Dagger component for the application. */
@Singleton
@Component(modules = {FirestoreModule.class, HandlerMapModule.class})
public interface ApplicationComponent {
  /** Provides a map of all Handlers by string. */
  Map<String, Provider<Handler>> handlerMap();

  /** Contains application and environment variables. */
  AppConfig appConfig();

  /** The primary interface for Discord systems. */
  DiscordSystem discordSystem();

  /** Need to build new Help Embed Dispatchers. */
  HelpEmbedDispatcher helpEmbedDispatcher();

  /** Handles all things related to the PubSub service. Creating, activating, etc. */
  PubSubManager pubSubManager();

  /** Executes the timed scraped result queue checking tasks. */
  ScrapedResultQueueDispatcher scrapedResultQueueDispatcher();

  /** Coordinate all the API checking to Pub/Sub publishing actions. */
  ScraperCoordinator scraperCoordinator();

  /** A factory for creating instances of the ScraperScheduler model. */
  ScraperSchedulerFactory scraperSchedulerFactory();

  /** Executes the timed stat logging tasks. */
  StatLogDispatcher statLogDispatcher();

  /** Handles shutting down, stopping, and deactivating. */
  ShutdownThread shutdownThread();
}
