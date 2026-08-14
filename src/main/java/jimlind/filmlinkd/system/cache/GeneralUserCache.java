package jimlind.filmlinkd.system.cache;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.factory.FirestoreUserFactory;
import jimlind.filmlinkd.google.db.FirestoreUserReader;
import jimlind.filmlinkd.model.FirestoreUser;

/** The General User Cache contains all active users. */
@Singleton
public class GeneralUserCache extends BaseUserCache {
  private final FirestoreUserReader userReader;

  /**
   * Constructor for this class.
   *
   * @param appConfig Holds all the configs that determine how the system run
   * @param userFactory Factory for creating {@link FirestoreUser} model
   * @param userReader Handles all read-only queries for user data from Firestore
   */
  @Inject
  public GeneralUserCache(
      AppConfig appConfig, FirestoreUserFactory userFactory, FirestoreUserReader userReader) {
    super(appConfig, userFactory);
    this.userReader = userReader;
  }

  @Override
  protected List<QueryDocumentSnapshot> getUserList() {
    return userReader.getActiveUsers();
  }
}
