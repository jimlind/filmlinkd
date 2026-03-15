package jimlind.filmlinkd.system.di;

import dagger.Component;
import java.util.Map;
import javax.inject.Provider;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.discord.dispatcher.HelpEmbedDispatcher;
import jimlind.filmlinkd.discord.event.handler.Handler;
import jimlind.filmlinkd.google.pubsub.PubSubManager;
import jimlind.filmlinkd.scraperv1.ScraperCoordinator;
import jimlind.filmlinkd.scraperv2.GeneralScraperDispatcher;
import jimlind.filmlinkd.scraperv2.VipScraperDispatcher;
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

  /** Executes the timed general user scraper. */
  GeneralScraperDispatcher generalScraperDispatcher();

  /** Executes the timed vip user scraper. */
  VipScraperDispatcher vipScraperDispatcher();

  /** Coordinate all the API checking to Pub/Sub publishing actions. */
  ScraperCoordinator scraperCoordinator();

  /** Executes the timed stat logging tasks. */
  StatLogDispatcher statLogDispatcher();

  /** Handles shutting down, stopping, and deactivating. */
  ShutdownThread shutdownThread();
}
