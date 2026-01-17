package jimlind.filmlinkd.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.google.secret.SecretManager;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

/** Contains application and environment variables. */
@Singleton
public class AppConfig {
  @Getter private final String applicationName;
  @Getter private final String applicationVersion;
  @Getter private final String discordApplicationId;
  @Getter private final String discordBotToken;
  @Getter private final String firestoreDatabaseId;
  @Getter private final String firestoreUserCollectionId;
  @Getter private final String firestoreVipCollectionId;
  @Getter private final String googleProjectId;
  @Getter private final String letterboxdApiKey;
  @Getter private final String letterboxdApiShared;
  @Getter private final String theMovieDatabaseApiKey;
  @Getter private final String pubSubCommandSubscriptionName;
  @Getter private final String pubSubCommandTopicName;
  @Getter private final String pubSubLogEntrySubscriptionName;
  @Getter private final String pubSubLogEntryTopicName;
  @Getter private final int scraperPaginationLimit;
  @Getter private final int scraperGeneralConcurrentLimit;
  @Getter private final int scraperGeneralPeriod;
  @Getter private final int scraperGeneralUserCachePeriod;
  @Getter private final int scraperVipConcurrentLimit;
  @Getter private final int scraperVipPeriod;
  @Getter private final int scraperVipUserCachePeriod;
  @Getter @Setter private String mainClass;

  @Inject
  AppConfig(SecretManager secretManager) {
    // Process application properties resource
    Properties appProperties = loadPropertiesFromResource("application.properties");

    // Load application properties directly
    applicationName = appProperties.getProperty(AppConstants.PROP_KEY_APPLICATION_NAME);
    applicationVersion = appProperties.getProperty(AppConstants.PROP_KEY_APPLICATION_VERSION);
    firestoreDatabaseId = appProperties.getProperty(AppConstants.PROP_KEY_FIRESTORE_DATABASE_ID);
    googleProjectId = appProperties.getProperty(AppConstants.PROP_KEY_GOOGLE_PROJECT_ID);

    // Load Secrets via names in application properties
    String letterboxdKeyName =
        appProperties.getProperty(AppConstants.PROP_KEY_LETTERBOXD_API_KEY_SECRET_NAME);
    String letterboxdSharedName =
        appProperties.getProperty(AppConstants.PROP_KEY_LETTERBOXD_API_SHARED_SECRET_NAME);
    String theMovieDatabaseApiKeyName =
        appProperties.getProperty(AppConstants.PROP_KEY_THE_MOVIE_DATABASE_API_KEY);
    letterboxdApiKey = secretManager.getSecret(googleProjectId, letterboxdKeyName);
    letterboxdApiShared = secretManager.getSecret(googleProjectId, letterboxdSharedName);
    theMovieDatabaseApiKey = secretManager.getSecret(googleProjectId, theMovieDatabaseApiKeyName);

    // Process environment properties resource
    @Nullable String environment = System.getenv("FILMLINKD_ENVIRONMENT");
    String env = "PRODUCTION".equals(environment) ? "prod" : "dev";
    String envResource = String.format("%s/environment.properties", env);
    Properties envProperties = loadPropertiesFromResource(envResource);

    // Load environment properties directly
    discordApplicationId = envProperties.getProperty(AppConstants.PROP_KEY_DISCORD_APPLICATION_ID);
    firestoreUserCollectionId =
        envProperties.getProperty(AppConstants.PROP_KEY_FIRESTORE_USER_COLLECTION_ID);
    firestoreVipCollectionId =
        envProperties.getProperty(AppConstants.PROP_KEY_FIRESTORE_VIP_COLLECTION_ID);
    pubSubCommandSubscriptionName =
        envProperties.getProperty(AppConstants.PROP_KEY_COMMAND_SUBSCRIPTION_NAME);
    pubSubCommandTopicName = envProperties.getProperty(AppConstants.PROP_KEY_COMMAND_TOPIC_NAME);
    pubSubLogEntrySubscriptionName =
        envProperties.getProperty(AppConstants.PROP_KEY_LOG_ENTRY_SUBSCRIPTION_NAME);
    pubSubLogEntryTopicName = envProperties.getProperty(AppConstants.PROP_KEY_LOG_ENTRY_TOPIC_NAME);
    scraperPaginationLimit =
        Integer.parseInt(envProperties.getProperty(AppConstants.PROP_KEY_SCRAPER_PAGINATION_LIMIT));
    scraperGeneralConcurrentLimit =
        Integer.parseInt(
            envProperties.getProperty(AppConstants.PROP_KEY_SCRAPER_GENERAL_CONCURRENT_LIMIT));
    scraperGeneralPeriod =
        Integer.parseInt(envProperties.getProperty(AppConstants.PROP_KEY_SCRAPER_GENERAL_PERIOD));
    scraperGeneralUserCachePeriod =
        Integer.parseInt(
            envProperties.getProperty(AppConstants.PROP_KEY_SCRAPER_GENERAL_USER_CACHE_PERIOD));
    scraperVipConcurrentLimit =
        Integer.parseInt(
            envProperties.getProperty(AppConstants.PROP_KEY_SCRAPER_VIP_CONCURRENT_LIMIT));
    scraperVipPeriod =
        Integer.parseInt(envProperties.getProperty(AppConstants.PROP_KEY_SCRAPER_VIP_PERIOD));
    scraperVipUserCachePeriod =
        Integer.parseInt(
            envProperties.getProperty(AppConstants.PROP_KEY_SCRAPER_VIP_USER_CACHE_PERIOD));

    // Load Secrets via names in environment properties
    discordBotToken =
        secretManager.getSecret(
            googleProjectId,
            envProperties.getProperty(AppConstants.PROP_KEY_DISCORD_BOT_TOKEN_SECRET_NAME));
  }

  private Properties loadPropertiesFromResource(String resourcePath) {
    Properties properties = new Properties();

    try (InputStream input = getClassLoader().getResourceAsStream(resourcePath)) {
      if (input == null) {
        throw new IllegalArgumentException(String.format("Empty %s found", resourcePath));
      }
      properties.load(input);
    } catch (IOException e) {
      throw new IllegalArgumentException(String.format("Error while loading %s", resourcePath), e);
    }

    return properties;
  }

  private ClassLoader getClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }
}
