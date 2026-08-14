package jimlind.filmlinkd.system;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.factory.FirestoreUserFactory;
import jimlind.filmlinkd.google.db.FirestoreUserReader;
import jimlind.filmlinkd.google.db.FirestoreUserWriter;
import jimlind.filmlinkd.model.FirestoreUser;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import org.jetbrains.annotations.Nullable;

/** Coordinates user actions that would otherwise require multiple method calls. */
@Singleton
public class FirestoreUserCoordinator {
  private final FirestoreUserFactory userFactory;
  private final FirestoreUserReader userReader;
  private final FirestoreUserWriter userWriter;

  /**
   * Constructor for this class.
   *
   * @param userFactory Builds the user object from a Firestore snapshot
   * @param userReader Handles all read-only queries for user data from Firestore
   * @param userWriter Handles all write operations for user data in Firestore
   */
  @Inject
  public FirestoreUserCoordinator(
      FirestoreUserFactory userFactory,
      FirestoreUserReader userReader,
      FirestoreUserWriter userWriter) {
    this.userFactory = userFactory;
    this.userReader = userReader;
    this.userWriter = userWriter;
  }

  /**
   * Update or create a user record adding the channel as a new subscription. Creates the user
   * record if it does not exist.
   *
   * @param member member
   * @param channelId channelid
   * @return user The created user or null if update failed
   */
  @Nullable
  public FirestoreUser follow(LbMember member, String channelId) {
    QueryDocumentSnapshot snapshot = userReader.getUserDocument(member.id);

    // Create the user in the database if it doesn't exist
    FirestoreUser user;
    if (snapshot == null) {
      user = userWriter.createUserDocument(member);
    } else {
      user = userFactory.createFromSnapshot(snapshot);
    }

    if (!userWriter.addUserSubscription(member.id, channelId)) {
      return null;
    }

    return user;
  }
}
