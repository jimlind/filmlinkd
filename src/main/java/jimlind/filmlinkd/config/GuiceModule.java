package jimlind.filmlinkd.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.system.DiscordSystem;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.discord.ConnectionManager;
import jimlind.filmlinkd.system.discord.EventListener;
import jimlind.filmlinkd.system.discord.SlashCommandManager;
import jimlind.filmlinkd.system.discord.eventHandler.ContributorHandler;
import jimlind.filmlinkd.system.discord.eventHandler.HelpHandler;
import jimlind.filmlinkd.system.google.SecretManager;

public class GuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    // Application Level Modules
    bind(AppConfig.class).in(Scopes.SINGLETON);
    bind(ShutdownThread.class);

    // Discord System Modules
    bind(DiscordSystem.class).in(Scopes.SINGLETON);
    bind(ConnectionManager.class).in(Scopes.SINGLETON);
    bind(EventListener.class).in(Scopes.SINGLETON);
    bind(SlashCommandManager.class).in(Scopes.SINGLETON);

    // Discord Event Handlers
    bind(ContributorHandler.class).in(Scopes.SINGLETON);
    bind(HelpHandler.class).in(Scopes.SINGLETON);

    // Google System Modules
    bind(SecretManager.class).in(Scopes.SINGLETON);
  }
}
