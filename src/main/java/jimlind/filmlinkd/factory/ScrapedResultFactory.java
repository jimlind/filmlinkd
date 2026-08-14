package jimlind.filmlinkd.factory;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.gson.GsonBuilder;
import com.google.pubsub.v1.PubsubMessage;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.google.db.FirestoreUserReader;
import jimlind.filmlinkd.model.FirestoreUser;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.ScrapedResult;
import lombok.extern.slf4j.Slf4j;

/** A factory for creating instances of the {@link ScrapedResult} model. */
@Singleton
@Slf4j
public class ScrapedResultFactory {
  private final FirestoreUserFactory userFactory;
  private final FirestoreUserReader userReader;

  /**
   * Constructor for the {@link ScrapedResultFactory}.
   *
   * @param userFactory Factory for creating {@link FirestoreUser} model
   * @param userReader Class that handles all read-only queries for user data from Firestore
   */
  @Inject
  ScrapedResultFactory(FirestoreUserFactory userFactory, FirestoreUserReader userReader) {
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
          .setMessage("Invalid FirestoreUser Passed in PubSub Message")
          .addKeyValue("message", message)
          .log();
      return null;
    }

    // Translate to user object
    FirestoreUser user = userFactory.createFromSnapshot(snapshot);
    if (user == null) {
      log.atWarn()
          .setMessage("Unable to Create FirestoreUser from Snapshot")
          .addKeyValue("message", message)
          .addKeyValue("snapshot", snapshot)
          .log();
      return null;
    }

    return new ScrapedResult(message, user);
  }
}
