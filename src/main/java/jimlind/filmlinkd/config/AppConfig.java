package jimlind.filmlinkd.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import jimlind.filmlinkd.system.google.SecretManager;
import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Singleton
public class AppConfig {
  private static final Properties properties = new Properties();
  @Getter private final String discordBotToken;
  @Getter private final String googleProjectId;

  @Inject
  AppConfig(SecretManager secretManager) {
    @Nullable String environment = System.getenv("FILMLINKD_ENVIRONMENT");
    String env = environment == null ? "dev" : environment.equals("PRODUCTION") ? "prod" : "dev";
    String resource = String.format("%s/environment.properties", env);

    try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(resource)) {
      if (input == null) {
        throw new RuntimeException("Empty properties file found");
      }
      properties.load(input);
    } catch (IOException e) {
      throw new RuntimeException("Error while loading properties file", e);
    }

    googleProjectId = properties.getProperty(AppConstants.PROP_KEY_GOOGLE_PROJECT_ID);

    discordBotToken =
        secretManager.getSecret(
            googleProjectId,
            properties.getProperty(AppConstants.PROP_KEY_DISCORD_BOT_TOKEN_SECRET_NAME));
  }
}
