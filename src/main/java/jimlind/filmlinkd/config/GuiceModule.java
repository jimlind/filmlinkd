package jimlind.filmlinkd.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import jimlind.filmlinkd.system.DiscordSystem;
import jimlind.filmlinkd.system.discord.ConnectionManager;
import jimlind.filmlinkd.system.google.SecretManager;

public class GuiceModule extends AbstractModule {
  @Override
  protected void configure() {
    bind(AppConfig.class).in(Scopes.SINGLETON);

    // Discord System Modules
    bind(DiscordSystem.class).in(Scopes.SINGLETON);
    bind(ConnectionManager.class).in(Scopes.SINGLETON);

    // Google System Modules
    bind(SecretManager.class).in(Scopes.SINGLETON);
  }
}
