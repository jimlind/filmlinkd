package jimlind.filmlinkd.cache;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Collections;
import java.util.List;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.google.firestore.UserReader;

/** The VIP User Cache contains all users in VIP servers. */
@Singleton
public class VipUserCache extends BaseUserCache {
  /**
   * Constructor for this class.
   *
   * @param appConfig Holds all the configs that determine how the system run
   * @param userFactory Factory for creating {@link User} model
   * @param userReader Handles all read-only queries for user data from Firestore
   */
  @Inject
  public VipUserCache(AppConfig appConfig, UserFactory userFactory, UserReader userReader) {
    super(appConfig, userFactory, userReader);
  }

  @Override
  protected List<QueryDocumentSnapshot> getUserList() {
    // TODO: Implement this
    return Collections.emptyList();
  }
}
