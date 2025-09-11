package jimlind.filmlinkd.system.google.pubsub;

import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.protobuf.Duration;
import com.google.pubsub.v1.ExpirationPolicy;
import com.google.pubsub.v1.ProjectName;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.SubscriptionName;
import com.google.pubsub.v1.TopicName;
import java.io.IOException;
import java.util.Objects;
import jimlind.filmlinkd.config.AppConfig;
import lombok.extern.slf4j.Slf4j;

/** Creates a PubSub subscription if it does not already exist. */
@Singleton
@Slf4j
public class SubscriptionCreator {
  static final int RETENTION_SECONDS = 43200; // 12 Hours
  static final int EXPIRATION_SECONDS = 86400; // 24 Hours
  static final int ACK_DEADLINE_SECONDS = 10;

  private final AppConfig appConfig;

  @Inject
  public SubscriptionCreator(AppConfig appConfig) {
    this.appConfig = appConfig;
  }

  /**
   * Creates a subscription if it does not already exist.
   *
   * @param subscriptionId The ID of the subscription
   * @param topicId The ID of the topic to subscribe to
   */
  public void createSubscriptionIfNotExist(String subscriptionId, String topicId) {
    String projectId = appConfig.getGoogleProjectId();
    ProjectName projectName = ProjectName.of(projectId);
    TopicName topicName = TopicName.of(projectId, topicId);
    SubscriptionName subscriptionName = SubscriptionName.of(projectId, subscriptionId);

    try (SubscriptionAdminClient client = SubscriptionAdminClient.create()) {
      if (!hasSubscription(client, projectName, subscriptionName)) {
        createSubscription(client, subscriptionName, topicName);
      }
    } catch (IOException e) {
      log.atError()
          .setMessage("Unable to create SubscriptionAdminClient")
          .addKeyValue("subscriptionId", subscriptionId)
          .addKeyValue("topicId", topicId)
          .setCause(e)
          .log();
    }
  }

  private void createSubscription(
      SubscriptionAdminClient client, SubscriptionName subscriptionName, TopicName topicName) {
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
