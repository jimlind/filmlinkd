package jimlind.filmlinkd.config.modules.discord;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.system.discord.embedbuilder.ContributorEmbedFactory;
import jimlind.filmlinkd.system.discord.embedbuilder.DiaryEntryEmbedFactory;
import jimlind.filmlinkd.system.discord.embedbuilder.DiaryListEmbedFactory;
import jimlind.filmlinkd.system.discord.embedbuilder.FilmEmbedFactory;
import jimlind.filmlinkd.system.discord.embedbuilder.FollowEmbedFactory;
import jimlind.filmlinkd.system.discord.embedbuilder.FollowingEmbedFactory;
import jimlind.filmlinkd.system.discord.embedbuilder.HelpEmbedFactory;
import jimlind.filmlinkd.system.discord.embedbuilder.ListEmbedFactory;
import jimlind.filmlinkd.system.discord.embedbuilder.LoggedEmbedFactory;
import jimlind.filmlinkd.system.discord.embedbuilder.RefreshEmbedFactory;
import jimlind.filmlinkd.system.discord.embedbuilder.UnfollowEmbedFactory;
import jimlind.filmlinkd.system.discord.embedbuilder.UserEmbedFactory;

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
