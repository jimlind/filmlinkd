package jimlind.filmlinkd.core.di;

import dagger.Component;
import java.util.Map;
import javax.inject.Provider;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.discord.event.handler.Handler;
import jimlind.filmlinkd.system.DiscordSystem;

/** Dagger component for the application. */
@Singleton
@Component(modules = {FirestoreModule.class, HandlerMapModule.class})
public interface ApplicationComponent {
  Map<String, Provider<Handler>> handlerMap();

  // Contains application and environment variables
  AppConfig appConfig();

  // The primary interface for Discord systems
  DiscordSystem discordSystem();
}
