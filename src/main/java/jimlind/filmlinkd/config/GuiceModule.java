package jimlind.filmlinkd.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.system.DiscordSystem;
import jimlind.filmlinkd.system.ShutdownThread;
import jimlind.filmlinkd.system.discord.ConnectionManager;
import jimlind.filmlinkd.system.discord.EventListener;
import jimlind.filmlinkd.system.discord.SlashCommandManager;
import jimlind.filmlinkd.system.discord.eventHandler.*;
import jimlind.filmlinkd.system.google.SecretManager;
import jimlind.filmlinkd.system.letterboxd.api.*;
import jimlind.filmlinkd.system.letterboxd.web.LetterboxdIdWeb;
import jimlind.filmlinkd.system.letterboxd.web.MemberWeb;

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
    bind(DiaryHandler.class).in(Scopes.SINGLETON);
    bind(FilmHandler.class).in(Scopes.SINGLETON);
    bind(HelpHandler.class).in(Scopes.SINGLETON);
    bind(ListHandler.class).in(Scopes.SINGLETON);
    bind(LoggedHandler.class).in(Scopes.SINGLETON);
    bind(RouletteHandler.class).in(Scopes.SINGLETON);
    bind(UserHandler.class).in(Scopes.SINGLETON);

    // Google System Modules
    bind(SecretManager.class).in(Scopes.SINGLETON);

    // Letterboxd API Modules
    bind(Client.class).in(Scopes.SINGLETON);
    bind(ContributorAPI.class).in(Scopes.SINGLETON);
    bind(FilmAPI.class).in(Scopes.SINGLETON);
    bind(ListAPI.class).in(Scopes.SINGLETON);
    bind(LogEntriesAPI.class).in(Scopes.SINGLETON);
    bind(MemberAPI.class).in(Scopes.SINGLETON);
    bind(MemberStatisticsAPI.class).in(Scopes.SINGLETON);
    // Letterboxd Web Scraper Modules
    bind(LetterboxdIdWeb.class).in(Scopes.SINGLETON);
    bind(MemberWeb.class).in(Scopes.SINGLETON);
  }
}
