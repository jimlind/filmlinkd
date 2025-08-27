package jimlind.filmlinkd.config.modules.discord;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.discord.factory.ContributorEmbedFactory;
import jimlind.filmlinkd.discord.factory.DiaryEntryEmbedFactory;
import jimlind.filmlinkd.discord.factory.DiaryListEmbedFactory;
import jimlind.filmlinkd.discord.factory.FilmEmbedFactory;
import jimlind.filmlinkd.discord.factory.FollowEmbedFactory;
import jimlind.filmlinkd.discord.factory.FollowingEmbedFactory;
import jimlind.filmlinkd.discord.factory.HelpEmbedFactory;
import jimlind.filmlinkd.discord.factory.ListEmbedFactory;
import jimlind.filmlinkd.discord.factory.LoggedEmbedFactory;
import jimlind.filmlinkd.discord.factory.RefreshEmbedFactory;
import jimlind.filmlinkd.discord.factory.UnfollowEmbedFactory;
import jimlind.filmlinkd.discord.factory.UserEmbedFactory;

/** Discord Embed Builder modules for dependency injection. */
public class EmbedFactoryModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ContributorEmbedFactory.class).in(Scopes.NO_SCOPE);
    bind(DiaryEntryEmbedFactory.class).in(Scopes.NO_SCOPE);
    bind(DiaryListEmbedFactory.class).in(Scopes.NO_SCOPE);
    bind(FilmEmbedFactory.class).in(Scopes.NO_SCOPE);
    bind(FollowEmbedFactory.class).in(Scopes.NO_SCOPE);
    bind(FollowingEmbedFactory.class).in(Scopes.NO_SCOPE);
    bind(HelpEmbedFactory.class).in(Scopes.NO_SCOPE);
    bind(ListEmbedFactory.class).in(Scopes.NO_SCOPE);
    bind(LoggedEmbedFactory.class).in(Scopes.NO_SCOPE);
    bind(RefreshEmbedFactory.class).in(Scopes.NO_SCOPE);
    bind(UnfollowEmbedFactory.class).in(Scopes.NO_SCOPE);
    bind(UserEmbedFactory.class).in(Scopes.NO_SCOPE);
  }
}
