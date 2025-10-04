package jimlind.filmlinkd.config.modules.discord;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.discord.event.handler.ContributorHandler;
import jimlind.filmlinkd.discord.event.handler.DiaryHandler;
import jimlind.filmlinkd.discord.event.handler.FilmHandler;
import jimlind.filmlinkd.discord.event.handler.FollowHandler;
import jimlind.filmlinkd.discord.event.handler.FollowingHandler;
import jimlind.filmlinkd.discord.event.handler.HelpHandler;
import jimlind.filmlinkd.discord.event.handler.ListHandler;
import jimlind.filmlinkd.discord.event.handler.LoggedHandler;
import jimlind.filmlinkd.discord.event.handler.RefreshHandler;
import jimlind.filmlinkd.discord.event.handler.RouletteHandler;
import jimlind.filmlinkd.discord.event.handler.UnfollowHandler;
import jimlind.filmlinkd.discord.event.handler.UserHandler;

/** Discord Event Handler modules for dependency injection. */
public class DiscordEventHandlerModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ContributorHandler.class).in(Scopes.SINGLETON);
    bind(DiaryHandler.class).in(Scopes.SINGLETON);
    bind(FilmHandler.class).in(Scopes.SINGLETON);
    bind(FollowHandler.class).in(Scopes.SINGLETON);
    bind(FollowingHandler.class).in(Scopes.SINGLETON);
    bind(HelpHandler.class).in(Scopes.SINGLETON);
    bind(ListHandler.class).in(Scopes.SINGLETON);
    bind(LoggedHandler.class).in(Scopes.SINGLETON);
    bind(RefreshHandler.class).in(Scopes.SINGLETON);
    bind(RouletteHandler.class).in(Scopes.SINGLETON);
    bind(UnfollowHandler.class).in(Scopes.SINGLETON);
    bind(UserHandler.class).in(Scopes.SINGLETON);
  }
}
