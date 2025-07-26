package jimlind.filmlinkd.config.modules.discord;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.system.discord.eventhandler.ContributorHandler;
import jimlind.filmlinkd.system.discord.eventhandler.DiaryHandler;
import jimlind.filmlinkd.system.discord.eventhandler.FilmHandler;
import jimlind.filmlinkd.system.discord.eventhandler.FollowHandler;
import jimlind.filmlinkd.system.discord.eventhandler.FollowingHandler;
import jimlind.filmlinkd.system.discord.eventhandler.HelpHandler;
import jimlind.filmlinkd.system.discord.eventhandler.ListHandler;
import jimlind.filmlinkd.system.discord.eventhandler.LoggedHandler;
import jimlind.filmlinkd.system.discord.eventhandler.RefreshHandler;
import jimlind.filmlinkd.system.discord.eventhandler.RouletteHandler;
import jimlind.filmlinkd.system.discord.eventhandler.UnfollowHandler;
import jimlind.filmlinkd.system.discord.eventhandler.UserHandler;

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
