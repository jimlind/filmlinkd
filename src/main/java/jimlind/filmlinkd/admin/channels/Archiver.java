package jimlind.filmlinkd.admin.channels;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.List;
import javax.inject.Inject;
import jimlind.filmlinkd.factory.FirestoreUserFactory;
import jimlind.filmlinkd.google.db.FirestoreUserReader;
import jimlind.filmlinkd.google.db.FirestoreUserWriter;
import jimlind.filmlinkd.model.FirestoreUser;

/** Archives a channel for all users following it. */
public class Archiver {
  private final FirestoreUserFactory userFactory;
  private final FirestoreUserReader userReader;
  private final FirestoreUserWriter userWriter;

  /**
   * Constructor for this class.
   *
   * @param userFactory Create user models
   * @param userReader Handles reading user records
   * @param userWriter Handles writing user records
   */
  @Inject
  public Archiver(
      FirestoreUserFactory userFactory,
      FirestoreUserReader userReader,
      FirestoreUserWriter userWriter) {
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
      FirestoreUser userModel = userFactory.createFromSnapshot(snapshot);
      if (userModel == null) {
        continue;
      }
      userWriter.archiveUserSubscription(userModel.getLetterboxdId(), channelId);
    }
  }
}
