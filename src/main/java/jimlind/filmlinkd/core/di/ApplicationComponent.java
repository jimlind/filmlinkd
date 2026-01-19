package jimlind.filmlinkd.core.di;

import dagger.Component;
import java.util.Map;
import javax.inject.Provider;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.discord.dispatcher.HelpEmbedDispatcher;
import jimlind.filmlinkd.discord.event.handler.Handler;
import jimlind.filmlinkd.google.pubsub.PubSubManager;
import jimlind.filmlinkd.system.DiscordSystem;
import jimlind.filmlinkd.system.dispatcher.StatLogDispatcher;

/** Dagger component for the application. */
@Singleton
@Component(modules = {FirestoreModule.class, HandlerMapModule.class})
public interface ApplicationComponent {
  Map<String, Provider<Handler>> handlerMap();

  // Contains application and environment variables
  AppConfig appConfig();

  // The primary interface for Discord systems
  DiscordSystem discordSystem();

  // Need to build new Help Embed Dispatchers
  HelpEmbedDispatcher helpEmbedDispatcher();

  // Handles all things related to the PubSub service. Creating, activating, etc.
  PubSubManager pubSubManager();

  // Executes the timed stat logging tasksa
  StatLogDispatcher statLogDispatcher();
}
