package jimlind.filmlinkd.google.pubsub;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.inject.Singleton;
import java.io.IOException;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.model.Command;
import jimlind.filmlinkd.model.Message;
import org.jetbrains.annotations.Nullable;

/** Handles all things related to the PubSub service. Creating, activating, etc. */
@Singleton
public class DummyPubSubManager implements PubSubManagerInterface {

  private final AppConfig appConfig;
  private final PublisherCreator publisherCreator;
//  private final TopicAdminClient topicClient;

  @Nullable private Publisher commandPublisher;

  DummyPubSubManager(AppConfig appConfig, PublisherCreator publisherCreator) throws IOException {
    this.appConfig = appConfig;
    this.publisherCreator = publisherCreator;
//    topicClient =
//        TopicAdminClient.create(
//            TopicAdminSettings.newBuilder()
//                .setCredentialsProvider(
//                    FixedCredentialsProvider.create(NoCredentials.getInstance()))
//                .build());
  }

  /** Builds all the needed publishers and subscribers. */
  public void buildAll() {
    buildCommandPublisher();
    buildLogEntryPublisher();
    buildCommandSubscriber();
    buildLogEntrySubscriber();
  }

  /** Builds the command publisher. */
  public void buildCommandPublisher() {
    commandPublisher = publisherCreator.create(appConfig.getPubSubCommandTopicName());
  }

  /** Builds the log entry publisher. */
  public void buildLogEntryPublisher() {}

  /** Builds the command subscriber. */
  public void buildCommandSubscriber() {}

  /** Builds the log entry subscriber. */
  public void buildLogEntrySubscriber() {}

  /** Deactivates the publishers and subscribers if they have been created. */
  public void deactivateAll() {}

  /**
   * Publishes a command to the command topic. Commands capture when users have performed a slash
   * command so all shards can react in the same way.
   *
   * @param command The command to translate to JSON then publish
   */
  public void publishCommand(Command command) {}

  /**
   * Publishes a log entry to the log entry topic. LogEntry captures when new entries are found
   * (usually by scraping) so that all shards can have a consistent state.
   *
   * @param logEntry The log entry to translate to JSON then publish
   */
  public void publishLogEntry(Message logEntry) {}
}
