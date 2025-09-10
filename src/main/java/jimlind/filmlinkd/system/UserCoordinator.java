package jimlind.filmlinkd.system;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.google.firestore.UserReader;
import jimlind.filmlinkd.system.google.firestore.UserWriter;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import org.jetbrains.annotations.Nullable;

public class UserCoordinator {
  private final UserFactory userFactory;
  private final UserReader userReader;
  private final UserWriter userWriter;

  /**
   * Constructor for this class.
   *
   * @param userFactory Builds the user object from a Firestore snapshot
   * @param userReader Handles all read-only queries for user data from Firestore
   * @param userWriter Handles all write operations for user data in Firestore
   */
  @Inject
  public UserCoordinator(UserFactory userFactory, UserReader userReader, UserWriter userWriter) {
    this.userFactory = userFactory;
    this.userReader = userReader;
    this.userWriter = userWriter;
  }

  @Nullable
  public User follow(LbMember member, String channelId) {
    QueryDocumentSnapshot snapshot = userReader.getUserDocument(member.id);

    // Create the user in the database if it doesn't exist
    User user;
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
