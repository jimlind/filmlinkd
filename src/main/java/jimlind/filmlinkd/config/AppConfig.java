package jimlind.filmlinkd.config;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import jimlind.filmlinkd.system.google.SecretManager;
import lombok.Getter;

@Singleton
public class AppConfig {
  private static final Properties properties = new Properties();
  @Getter private final String discordBotToken;

  @Inject
  AppConfig(SecretManager secretManager) {
    String environment = "DEVELOPMENT";
    String resourcesDir = environment.equals("PRODUCTION") ? "prod" : "dev";
    String resource = String.format("%s/environment.properties", resourcesDir);

    try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(resource)) {
      if (input == null) {
        throw new RuntimeException("Empty properties file found");
      }
      properties.load(input);
    } catch (IOException e) {
      throw new RuntimeException("Error while loading properties file", e);
    }

    discordBotToken =
        secretManager.getSecret(
            properties.getProperty(AppConstants.PROP_KEY_DISCORD_BOT_TOKEN_SECRET_NAME));
  }
}
