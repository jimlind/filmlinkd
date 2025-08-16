package jimlind.filmlinkd.factory;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.pubsub.v1.PubsubMessage;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.ScrapedResult;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.google.firestore.UserReader;
import lombok.extern.slf4j.Slf4j;

/** A factory for creating instances of the {@link ScrapedResult} model. */
@Slf4j
public class ScrapedResultFactory {
  private final UserFactory userFactory;
  private final UserReader userReader;

  /**
   * Constructor for the {@link ScrapedResultFactory}.
   *
   * @param userFactory Factory for creating {@link User} model
   * @param userReader Class that handles all read-only queries for user data from Firestore
   */
  @Inject
  public ScrapedResultFactory(UserFactory userFactory, UserReader userReader) {
    this.userFactory = userFactory;
    this.userReader = userReader;
  }

  private static Message translateMessage(PubsubMessage pubsubMessage) {
    String data = pubsubMessage.getData().toStringUtf8();
    return new GsonBuilder().create().fromJson(data, Message.class);
  }

  /**
   * Attempts to translate a message from PubSub to a {@link ScrapedResult} model that we can use.
   *
   * @param pubsubMessage The message from PubSub
   * @return A {@link ScrapedResult} or null if unable to create
   */
  public ScrapedResult createFromPubSubMessage(PubsubMessage pubsubMessage) {
    Message message = translateMessage(pubsubMessage);

    // Attempt to get user based on Message
    QueryDocumentSnapshot snapshot = userReader.getUserDocument(message.getEntryUserLid());
    if (snapshot == null) {
      log.atWarn()
          .setMessage("Invalid User Passed in PubSub Message")
          .addKeyValue("message", message)
          .log();
      return null;
    }

    // Translate to user object
    User user = userFactory.createFromSnapshot(snapshot);
    if (user == null) {
      log.atWarn()
          .setMessage("Unable to Create User from Snapshot")
          .addKeyValue("message", message)
          .addKeyValue("snapshot", snapshot)
          .log();
      return null;
    }

    return new ScrapedResult(message, user);
  }
}
