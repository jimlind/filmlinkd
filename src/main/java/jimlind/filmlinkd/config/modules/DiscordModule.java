package jimlind.filmlinkd.config.modules;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.config.modules.discord.DiscordEmbedBuilderModule;
import jimlind.filmlinkd.config.modules.discord.DiscordEventHandlerModule;
import jimlind.filmlinkd.config.modules.discord.DiscordStringBuilderModule;
import jimlind.filmlinkd.system.DiscordSystem;
import jimlind.filmlinkd.system.discord.ConnectionManager;
import jimlind.filmlinkd.system.discord.EventListener;
import jimlind.filmlinkd.system.discord.ShardManagerStorage;
import jimlind.filmlinkd.system.discord.SlashCommandManager;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;

/** Discord modules for dependency injection. */
public class DiscordModule extends AbstractModule {
  @Override
  protected void configure() {
    // System
    bind(DiscordSystem.class).in(Scopes.SINGLETON);
    bind(ConnectionManager.class).in(Scopes.SINGLETON);
    bind(EventListener.class).in(Scopes.SINGLETON);
    bind(ShardManagerStorage.class).in(Scopes.SINGLETON);
    bind(SlashCommandManager.class).in(Scopes.SINGLETON);

    // Helpers
    bind(AccountHelper.class).in(Scopes.SINGLETON);
    bind(ChannelHelper.class).in(Scopes.SINGLETON);

    // Builder and Handler Collections
    install(new DiscordEmbedBuilderModule());
    install(new DiscordEventHandlerModule());
    install(new DiscordStringBuilderModule());
  }
}
