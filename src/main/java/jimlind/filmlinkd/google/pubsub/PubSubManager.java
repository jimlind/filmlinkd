package jimlind.filmlinkd.google.pubsub;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.SubscriptionName;
import java.util.concurrent.ExecutionException;
import javax.inject.Inject;
import javax.inject.Singleton;
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

  private final AppConfig appConfig;
  private final CommandMessageReceiver commandMessageReceiver;
  private final LogEntryMessageReceiver logEntryMessageReceiver;
  private final PublisherCreator publisherCreator;
  private final SubscriberListener subscriberListener;
  private final SubscriptionCreator subscriptionCreator;

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
   * @param publisherCreator Creates publishers for PubSub
   * @param subscriberListener Listens for PubSub messages
   * @param subscriptionCreator Creates subscriptions if they do not exist
   */
  @Inject
  PubSubManager(
      AppConfig appConfig,
      CommandMessageReceiver commandMessageReceiver,
      LogEntryMessageReceiver logEntryMessageReceiver,
      PublisherCreator publisherCreator,
      SubscriberListener subscriberListener,
      SubscriptionCreator subscriptionCreator) {
    this.appConfig = appConfig;
    this.commandMessageReceiver = commandMessageReceiver;
    this.logEntryMessageReceiver = logEntryMessageReceiver;
    this.publisherCreator = publisherCreator;
    this.subscriberListener = subscriberListener;
    this.subscriptionCreator = subscriptionCreator;
  }

  /** Builds the command publisher. */
  public void buildCommandPublisher() {
    commandPublisher = publisherCreator.create(appConfig.getPubSubCommandTopicName());
  }

  /** Builds the log entry publisher. */
  public void buildLogEntryPublisher() {
    logEntryPublisher = publisherCreator.create(appConfig.getPubSubLogEntryTopicName());
  }

  /** Builds the command subscriber. */
  public void buildCommandSubscriber() {
    String projectId = appConfig.getGoogleProjectId();
    String subscriptionId = appConfig.getPubSubCommandSubscriptionName();
    String topicId = appConfig.getPubSubCommandTopicName();
    String subscriptionName = SubscriptionName.of(projectId, subscriptionId).toString();

    subscriptionCreator.createSubscriptionIfNotExist(subscriptionId, topicId);
    commandSubscriber = Subscriber.newBuilder(subscriptionName, commandMessageReceiver).build();
    subscribeListener(commandSubscriber);
  }

  /** Builds the log entry subscriber. */
  public void buildLogEntrySubscriber() {
    String projectId = appConfig.getGoogleProjectId();
    String subscriptionId = appConfig.getPubSubLogEntrySubscriptionName();
    String topicId = appConfig.getPubSubLogEntryTopicName();
    String subscriptionName = SubscriptionName.of(projectId, subscriptionId).toString();

    subscriptionCreator.createSubscriptionIfNotExist(subscriptionId, topicId);
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
          .setCause(e)
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
          .setCause(e)
          .log();
    }
  }

  private void subscribeListener(Subscriber subscriber) {
    String subscriptionName = subscriber.getSubscriptionNameString();
    try {
      subscriber.addListener(subscriberListener, MoreExecutors.directExecutor());
      subscriber.startAsync().awaitRunning();
      log.info("Started Listening for Messages on {}", subscriptionName);
    } catch (IllegalStateException ignore) {
      log.error("Failed Starting Listening for Messages on {}", subscriptionName);
    }
  }
}
