package jimlind.filmlinkd.config.modules.discord;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.system.discord.embedbuilder.ContributorEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.DiaryEntryEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.DiaryListEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.FilmEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.FollowEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.FollowingEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.HelpEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.ListEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.LoggedEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.RefreshEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.UnfollowEmbedBuilder;
import jimlind.filmlinkd.system.discord.embedbuilder.UserEmbedBuilder;

/** Discord Embed Builder modules for dependency injection. */
public class DiscordEmbedBuilderModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(ContributorEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(DiaryEntryEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(DiaryListEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(FilmEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(FollowEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(FollowingEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(HelpEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(ListEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(LoggedEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(RefreshEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(UnfollowEmbedBuilder.class).in(Scopes.NO_SCOPE);
    bind(UserEmbedBuilder.class).in(Scopes.NO_SCOPE);
  }
}
