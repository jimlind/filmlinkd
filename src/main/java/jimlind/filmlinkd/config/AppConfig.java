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
  private static final Properties appProperties = new Properties();
  private static final Properties envProperties = new Properties();

  @Getter private final String applicationName;
  @Getter private final String applicationVersion;

  @Getter private final String discordBotToken;
  @Getter private final String firestoreCollectionId;
  @Getter private final String googleProjectId;
  @Getter private final String letterboxdApiKey;
  @Getter private final String letterboxdApiShared;
  @Getter private final String pubSubCommandSubscriptionName;
  @Getter private final String pubSubCommandTopicName;
  @Getter private final String pubSubLogEntrySubscriptionName;
  @Getter private final String pubSubLogEntryTopicName;

  @Inject
  AppConfig(SecretManager secretManager) {
    @Nullable String environment = System.getenv("FILMLINKD_ENVIRONMENT");
    String env = environment == null ? "dev" : environment.equals("PRODUCTION") ? "prod" : "dev";
    String envResource = String.format("%s/environment.properties", env);

    try (InputStream input = AppConfig.class.getClassLoader().getResourceAsStream(envResource)) {
      if (input == null) {
        throw new IllegalArgumentException("Empty environment properties file found");
      }
      envProperties.load(input);
    } catch (IOException e) {
      // Because this might only happen on initialization throw an argument exception
      throw new IllegalArgumentException("Error while loading environment properties file", e);
    }

    try (InputStream input =
        AppConfig.class.getClassLoader().getResourceAsStream("application.properties")) {
      if (input == null) {
        throw new IllegalArgumentException("Empty application properties file found");
      }
      appProperties.load(input);
    } catch (IOException e) {
      // Because this might only happen on initialization throw an argument exception
      throw new IllegalArgumentException("Error while loading application properties file", e);
    }

    firestoreCollectionId =
        envProperties.getProperty(AppConstants.PROP_KEY_FIRESTORE_COLLECTION_ID);
    googleProjectId = envProperties.getProperty(AppConstants.PROP_KEY_GOOGLE_PROJECT_ID);
    pubSubCommandSubscriptionName =
        envProperties.getProperty(AppConstants.PROP_KEY_COMMAND_SUBSCRIPTION_NAME);
    pubSubCommandTopicName = envProperties.getProperty(AppConstants.PROP_KEY_COMMAND_TOPIC_NAME);
    pubSubLogEntrySubscriptionName =
        envProperties.getProperty(AppConstants.PROP_KEY_LOG_ENTRY_SUBSCRIPTION_NAME);
    pubSubLogEntryTopicName = envProperties.getProperty(AppConstants.PROP_KEY_LOG_ENTRY_TOPIC_NAME);

    discordBotToken =
        secretManager.getSecret(
            googleProjectId,
            envProperties.getProperty(AppConstants.PROP_KEY_DISCORD_BOT_TOKEN_SECRET_NAME));

    letterboxdApiKey =
        secretManager.getSecret(
            googleProjectId,
            envProperties.getProperty(AppConstants.PROP_KEY_LETTERBOXD_API_KEY_SECRET_NAME));

    letterboxdApiShared =
        secretManager.getSecret(
            googleProjectId,
            envProperties.getProperty(AppConstants.PROP_KEY_LETTERBOXD_API_SHARED_SECRET_NAME));

    applicationName = appProperties.getProperty("app.name");
    applicationVersion = appProperties.getProperty("app.version");
  }
}
