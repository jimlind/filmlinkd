package jimlind.filmlinkd.system.google;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.ByteString;
import com.google.protobuf.Duration;
import com.google.pubsub.v1.ExpirationPolicy;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.model.Command;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.MessageReceiver;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

/** Handles all things related to the PubSub service. Creating, activating, etc. */
@Singleton
@Slf4j
public class PubSubManager {
  public static final String EXCEPTION_KEY = "exception";

  static final int RETENTION_SECONDS = 43200; // 12 Hours
  static final int EXPIRATION_SECONDS = 86400; // 24 Hours
  static final int ACK_DEADLINE_SECONDS = 10;

  private final AppConfig appConfig;
  private final MessageReceiver messageReceiver;
  private final PubSubSubscriberListener pubSubSubscriberListener;

  @Nullable private Publisher logEntryPublisher;
  @Nullable private Publisher commandPublisher;
  @Nullable private Subscriber logEntrySubscriber;
  @Nullable private Subscriber commandSubscriber;

  /**
   * The constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   * @param messageReceiver Receives the PubSub message object
   * @param pubSubSubscriberListener Listens for PubSub messages
   */
  @Inject
  PubSubManager(
      AppConfig appConfig,
      MessageReceiver messageReceiver,
      PubSubSubscriberListener pubSubSubscriberListener) {
    this.appConfig = appConfig;
    this.messageReceiver = messageReceiver;
    this.pubSubSubscriberListener = pubSubSubscriberListener;
  }

  /** Builds all the needed publishers and subscribers. */
  public void activate() {
    buildCommandPublisher();
    buildLogEntryPublisher();

    logEntrySubscriber =
        buildSubscriber(
            appConfig.getGoogleProjectId(),
            appConfig.getPubSubLogEntrySubscriptionName(),
            appConfig.getPubSubLogEntryTopicName());
  }

  /** Builds the command publisher. */
  public void buildCommandPublisher() {
    commandPublisher =
        buildPublisher(appConfig.getGoogleProjectId(), appConfig.getPubSubCommandTopicName());
  }

  /** Builds the log entry publisher. */
  public void buildLogEntryPublisher() {
    logEntryPublisher =
        buildPublisher(appConfig.getGoogleProjectId(), appConfig.getPubSubLogEntryTopicName());
  }

  /** Deactivates the publishers and subscribers if they have been created. */
  public void deactivate() {
    if (logEntrySubscriber != null) {
      log.atInfo()
          .setMessage("Stopping PubSub Subscriber for Messages on {}")
          .addArgument(logEntrySubscriber.getSubscriptionNameString())
          .log();
      logEntrySubscriber.stopAsync();
    }

    if (logEntryPublisher != null) {
      if (log.isInfoEnabled()) {
        log.info("Stopping PubSub Publisher on {}", logEntryPublisher.getTopicNameString());
      }
      logEntryPublisher.shutdown();
    }

    if (commandPublisher != null) {
      if (log.isInfoEnabled()) {
        log.info("Stopping PubSub Publisher on {}", commandPublisher.getTopicNameString());
      }
      commandPublisher.shutdown();
    }
  }

  /**
   * Publishes a command to the command topic. Commands capture when users have performed a slash
   * command so all shards can react in the same way.
   *
   * @param command The command to translate to JSON then publish
   */
  public void publishCommand(Command command) {
    if (commandPublisher == null) {
      return;
    }

    ByteString data = ByteString.copyFromUtf8(command.toJson());
    PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
    // TODO: Check what sort of exception I can actually get out of here.
    try {
      commandPublisher.publish(pubsubMessage).get();
    } catch (InterruptedException | ExecutionException e) {
      log.atWarn()
          .setMessage("Unable to Publish command")
          .addKeyValue("command", command)
          .addKeyValue(EXCEPTION_KEY, e)
          .log();
    }
  }

  /**
   * Publishes a log entry to the log entry topic. LogEntry captures when new entries are found
   * (usually by scraping) so that all shards can have a consistent state.
   *
   * @param logEntry The log entry to translate to JSON then publish
   */
  public void publishLogEntry(Message logEntry) {
    if (logEntryPublisher == null) {
      return;
    }

    ByteString data = ByteString.copyFromUtf8(logEntry.toJson());
    PubsubMessage pubsubMessage = PubsubMessage.newBuilder().setData(data).build();
    // TODO: Check what sort of exception I can actually get out of here.
    try {
      logEntryPublisher.publish(pubsubMessage).get();
    } catch (InterruptedException | ExecutionException e) {
      log.atWarn()
          .setMessage("Unable to Publish logEntry")
          .addKeyValue("logEntry", logEntry)
          .addKeyValue(EXCEPTION_KEY, e)
          .log();
    }
  }

  private Publisher buildPublisher(String projectId, String topicId) {
    TopicName topicName = TopicName.of(projectId, topicId);
    // TODO: Check what sort of exception I can actually get out of here.
    try {
      return Publisher.newBuilder(topicName).build();
    } catch (IOException e) {
      log.atError()
          .setMessage("Unable to build Publisher {}")
          .addArgument(topicId)
          .addKeyValue(EXCEPTION_KEY, e)
          .log();
      return null;
    }
  }

  private Subscriber buildSubscriber(String projectId, String subscriptionId, String topicId) {
    ProjectName projectName = ProjectName.of(projectId);
    TopicName topicName = TopicName.of(projectId, topicId);
    SubscriptionName subscriptionName = SubscriptionName.of(projectId, subscriptionId);

    // TODO: Check what sort of exception I can actually get out of here.
    // This client create is designed specifically for a try-with-resources statement
    try (SubscriptionAdminClient client = SubscriptionAdminClient.create()) {
      // If the subscription doesn't exit, create it.
      if (!hasSubscription(client, projectName, subscriptionName)) {
        createSubscription(client, subscriptionName, topicName);
      }
    } catch (IOException e) {
      log.atError()
          .setMessage("Unable to setup connection to the PubSub client")
          .addKeyValue(EXCEPTION_KEY, e)
          .log();
      return null;
    }

    // Build a subscriber wired up to a message receivers and event listeners
    // If we are going to properly expand this to be a method that creates generic subscribers this
    // will have to be rebuilt.
    Subscriber subscriber =
        Subscriber.newBuilder(subscriptionName.toString(), messageReceiver).build();
    subscriber.addListener(pubSubSubscriberListener, MoreExecutors.directExecutor());

    subscriber.startAsync().awaitRunning();
    log.info("Starting Listening for Messages on {}", subscriptionName);

    return subscriber;
  }

  private void createSubscription(
      SubscriptionAdminClient client, SubscriptionName subscriptionName, TopicName topicName) {
    // Setup duration and expirations
    Duration retentionDuration = Duration.newBuilder().setSeconds(RETENTION_SECONDS).build();
    Duration expirationDuration = Duration.newBuilder().setSeconds(EXPIRATION_SECONDS).build();
    ExpirationPolicy expirationPolicy =
        ExpirationPolicy.newBuilder().setTtl(expirationDuration).build();

    Subscription.Builder builder =
        Subscription.newBuilder()
            .setName(subscriptionName.toString())
            .setTopic(topicName.toString())
            .setAckDeadlineSeconds(ACK_DEADLINE_SECONDS)
            .setMessageRetentionDuration(retentionDuration)
            .setExpirationPolicy(expirationPolicy);

    client.createSubscription(builder.build());
  }

  private boolean hasSubscription(
      SubscriptionAdminClient client, ProjectName projectName, SubscriptionName subscriptionName) {
    String project = projectName.toString();
    for (Subscription subscription : client.listSubscriptions(project).iterateAll()) {
      if (Objects.equals(subscription.getName(), subscriptionName.toString())) {
        return true;
      }
    }
    return false;
  }
}
