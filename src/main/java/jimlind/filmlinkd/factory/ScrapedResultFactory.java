package jimlind.filmlinkd.factory;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.gson.GsonBuilder;
import com.google.inject.Inject;
import com.google.pubsub.v1.PubsubMessage;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.ScrapedResult;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.google.FirestoreManager;
import lombok.extern.slf4j.Slf4j;

/** A factory for creating instances of the {@link ScrapedResult} model. */
@Slf4j
public class ScrapedResultFactory {
  private final FirestoreManager firestoreManager;
  private final UserFactory userFactory;

  /**
   * Constructor for the {@link ScrapedResultFactory}.
   *
   * @param firestoreManager Class that handles all Firestore interactions
   * @param userFactory Factory for creating {@link User} model
   */
  @Inject
  public ScrapedResultFactory(FirestoreManager firestoreManager, UserFactory userFactory) {
    this.firestoreManager = firestoreManager;
    this.userFactory = userFactory;
  }

  /**
   * Attempts to translate a message from PubSub to a {@link ScrapedResult} model that we can use.
   *
   * @param pubsubMessage The message from PubSub
   * @return A {@link ScrapedResult} or null if unable to create
   */
  public ScrapedResult createFromPubSubMessage(PubsubMessage pubsubMessage) {
    // Translate to a message object
    String data = pubsubMessage.getData().toStringUtf8();
    Message message = new GsonBuilder().create().fromJson(data, Message.class);

    // Attempt to get user based on Message
    String userLid = message.getEntry().getUserLid();
    QueryDocumentSnapshot snapshot = firestoreManager.getUserDocument(userLid);
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

    ScrapedResult scrapedResult = new ScrapedResult();
    scrapedResult.setMessage(message);
    scrapedResult.setUser(user);

    return scrapedResult;
  }
}
