package jimlind.filmlinkd.system.google.pubsub;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import jimlind.filmlinkd.config.AppConfig;
import lombok.extern.slf4j.Slf4j;

/** Creates a PubSub publisher. */
@Singleton
@Slf4j
public class PublisherCreator {
  private final AppConfig appConfig;

  /**
   * Constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   */
  @Inject
  public PublisherCreator(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  /**
   * Creates a publisher.
   *
   * @param topicId The name of the topic to build a publisher for
   * @return The build publisher
   */
  public Publisher create(String topicId) {
    TopicName topicName = TopicName.of(appConfig.getGoogleProjectId(), topicId);
    try {
      return Publisher.newBuilder(topicName).build();
    } catch (IOException e) {
      log.atError()
          .setMessage("Unable to build Publisher {}")
          .addArgument(topicId)
          .setCause(e)
          .log();
      return null;
    }
  }
}
