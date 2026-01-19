package jimlind.filmlinkd.admin;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.inject.Inject;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.google.db.UserReader;
import jimlind.filmlinkd.google.db.UserWriter;
import jimlind.filmlinkd.model.User;

/** Admin command to clean up user records. */
public class UndoChannelArchive {

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
  public UndoChannelArchive(UserFactory userFactory, UserReader userReader, UserWriter userWriter) {
    this.userFactory = userFactory;
    this.userReader = userReader;
    this.userWriter = userWriter;
  }

  /**
   * For all users with a specific archived channel id, move it to the subscription list.
   *
   * @param channelId Archived channel id.
   */
  public void run(String channelId) {
    List<QueryDocumentSnapshot> userSnapshotList =
        userReader.getUserDocumentListByArchivedChannelId(channelId);

    try (PrintWriter out =
        new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true)) {
      for (QueryDocumentSnapshot snapshot : userSnapshotList) {
        User user = userFactory.createFromSnapshot(snapshot);
        if (user != null) {
          out.println(user.userName);
          boolean result =
              userWriter.revertUserArchivedSubscription(user.getLetterboxdId(), channelId);
          if (!result) {
            out.println("FAILURE");
            break;
          }
        }
      }
    }
  }
}
