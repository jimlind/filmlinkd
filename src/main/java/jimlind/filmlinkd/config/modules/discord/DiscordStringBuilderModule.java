package jimlind.filmlinkd.config.modules.discord;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.system.discord.stringbuilder.CountStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.DirectorsStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.RuntimeStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.StarsStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.TextStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.UserStringBuilder;

/** Discord String Builder modules for dependency injection. */
public class DiscordStringBuilderModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(CountStringBuilder.class).in(Scopes.NO_SCOPE);
    bind(DescriptionStringBuilder.class).in(Scopes.NO_SCOPE);
    bind(DirectorsStringBuilder.class).in(Scopes.NO_SCOPE);
    bind(RuntimeStringBuilder.class).in(Scopes.NO_SCOPE);
    bind(StarsStringBuilder.class).in(Scopes.NO_SCOPE);
    bind(TextStringBuilder.class).in(Scopes.NO_SCOPE);
    bind(UserStringBuilder.class).in(Scopes.NO_SCOPE);
  }
}
