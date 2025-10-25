package jimlind.filmlinkd.admin.channels;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import java.util.List;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.google.db.UserReader;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.google.firestore.UserWriter;

/** Archives a channel for all users following it. */
public class Archiver {
  private final UserFactory userFactory;
  private final UserReader userReader;
  private final UserWriter userWriter;

  /**
   * Constructor for this class.
   *
   * @param userFactory Create user models
   * @param userReader Handles reading user records
   * @param userWriter Handles writing user records
   */
  @Inject
  public Archiver(UserFactory userFactory, UserReader userReader, UserWriter userWriter) {
    this.userFactory = userFactory;
    this.userReader = userReader;
    this.userWriter = userWriter;
  }

  /**
   * Calls the user writer to archive a channel for all users following it.
   *
   * @param channelId Channel to archive
   */
  public void archive(String channelId) {
    List<QueryDocumentSnapshot> snapshotList = userReader.getUserDocumentListByChannelId(channelId);
    for (QueryDocumentSnapshot snapshot : snapshotList) {
      User userModel = userFactory.createFromSnapshot(snapshot);
      if (userModel == null) {
        continue;
      }
      userWriter.archiveUserSubscription(userModel.getLetterboxdId(), channelId);
    }
  }
}
