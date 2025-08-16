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
import jimlind.filmlinkd.reciever.CommandMessageReceiver;
import jimlind.filmlinkd.reciever.LogEntryMessageReceiver;
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
  private final CommandMessageReceiver commandMessageReceiver;
  private final LogEntryMessageReceiver logEntryMessageReceiver;
  private final PubSubSubscriberListener pubSubSubscriberListener;

  @Nullable private Publisher commandPublisher;
  @Nullable private Publisher logEntryPublisher;
  @Nullable private Subscriber commandSubscriber;
  @Nullable private Subscriber logEntrySubscriber;

  /**
   * The constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   * @param commandMessageReceiver Receives the PubSub object for command messages
   * @param logEntryMessageReceiver Receives the PubSub object for logentry messages
   * @param pubSubSubscriberListener Listens for PubSub messages
   */
  @Inject
  PubSubManager(
      AppConfig appConfig,
      CommandMessageReceiver commandMessageReceiver,
      LogEntryMessageReceiver logEntryMessageReceiver,
      PubSubSubscriberListener pubSubSubscriberListener) {
    this.appConfig = appConfig;
    this.commandMessageReceiver = commandMessageReceiver;
    this.logEntryMessageReceiver = logEntryMessageReceiver;
    this.pubSubSubscriberListener = pubSubSubscriberListener;
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
    commandPublisher =
        buildPublisher(appConfig.getGoogleProjectId(), appConfig.getPubSubCommandTopicName());
  }

  /** Builds the log entry publisher. */
  public void buildLogEntryPublisher() {
    logEntryPublisher =
        buildPublisher(appConfig.getGoogleProjectId(), appConfig.getPubSubLogEntryTopicName());
  }

  /** Builds the command subscriber. */
  public void buildCommandSubscriber() {
    String projectId = appConfig.getGoogleProjectId();
    String subscriptionId = appConfig.getPubSubCommandSubscriptionName();
    String subscriptionName = SubscriptionName.of(projectId, subscriptionId).toString();

    createSubscriptionClient(projectId, subscriptionId, appConfig.getPubSubCommandTopicName());
    commandSubscriber = Subscriber.newBuilder(subscriptionName, commandMessageReceiver).build();
    subscribeListener(commandSubscriber);
  }

  /** Builds the log entry subscriber. */
  public void buildLogEntrySubscriber() {
    String projectId = appConfig.getGoogleProjectId();
    String subscriptionId = appConfig.getPubSubLogEntrySubscriptionName();
    String subscriptionName = SubscriptionName.of(projectId, subscriptionId).toString();

    createSubscriptionClient(projectId, subscriptionId, appConfig.getPubSubLogEntryTopicName());
    logEntrySubscriber = Subscriber.newBuilder(subscriptionName, logEntryMessageReceiver).build();
    subscribeListener(logEntrySubscriber);
  }

  /** Deactivates the publishers and subscribers if they have been created. */
  public void deactivateAll() {
    if (commandPublisher != null) {
      if (log.isInfoEnabled()) {
        log.info("Stopping Command PubSub Publisher on {}", commandPublisher.getTopicNameString());
      }
      commandPublisher.shutdown();
    }

    if (logEntryPublisher != null) {
      if (log.isInfoEnabled()) {
        log.info(
            "Stopping Log Entry PubSub Publisher on {}", logEntryPublisher.getTopicNameString());
      }
      logEntryPublisher.shutdown();
    }

    if (commandSubscriber != null) {
      log.atInfo()
          .setMessage("Stopping PubSub Subscriber for Messages on {}")
          .addArgument(commandSubscriber.getSubscriptionNameString())
          .log();
      commandSubscriber.stopAsync();
    }

    if (logEntrySubscriber != null) {
      log.atInfo()
          .setMessage("Stopping PubSub Subscriber for Messages on {}")
          .addArgument(logEntrySubscriber.getSubscriptionNameString())
          .log();
      logEntrySubscriber.stopAsync();
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

  private void createSubscriptionClient(String projectId, String subscriptionId, String topicId) {
    ProjectName projectName = ProjectName.of(projectId);
    TopicName topicName = TopicName.of(projectId, topicId);
    SubscriptionName subscriptionName = SubscriptionName.of(projectId, subscriptionId);

    // This client create is designed specifically for a try-with-resources statement
    try (SubscriptionAdminClient client = SubscriptionAdminClient.create()) {
      // If the subscription doesn't exit, create it.
      if (!hasSubscription(client, projectName, subscriptionName)) {
        createSubscription(client, subscriptionName, topicName);
      }
    } catch (IOException e) {
      log.atError()
          .setMessage("Unable to create SubscriptionAdminClient")
          .addKeyValue("subscriptionId", subscriptionId)
          .addKeyValue("topicId", topicId)
          .addKeyValue(EXCEPTION_KEY, e)
          .log();
    }
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

  private void subscribeListener(Subscriber subscriber) {
    subscriber.addListener(pubSubSubscriberListener, MoreExecutors.directExecutor());
    subscriber.startAsync().awaitRunning();
    if (log.isInfoEnabled()) {
      log.info("Starting Listening for Messages on {}", subscriber.getSubscriptionNameString());
    }
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
