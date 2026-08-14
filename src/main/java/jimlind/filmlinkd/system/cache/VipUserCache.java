package jimlind.filmlinkd.system.cache;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.factory.FirestoreUserFactory;
import jimlind.filmlinkd.factory.VipFactory;
import jimlind.filmlinkd.google.db.FirestoreUserReader;
import jimlind.filmlinkd.google.db.VipReader;
import jimlind.filmlinkd.model.FirestoreUser;
import jimlind.filmlinkd.model.Vip;

/** The VIP User Cache contains all users in VIP servers. */
@Singleton
public class VipUserCache extends BaseUserCache {
  private final FirestoreUserReader userReader;
  private final VipFactory vipFactory;
  private final VipReader vipReader;

  /**
   * Constructor for this class.
   *
   * @param appConfig Holds all the configs that determine how the system run
   * @param userFactory Factory for creating {@link FirestoreUser} model
   * @param userReader Handles all read-only queries for user data from Firestore
   * @param vipFactory Factory for creating {@link Vip} model
   * @param vipReader Handles all read-only queries for VIP data from Firestore
   */
  @Inject
  public VipUserCache(
      AppConfig appConfig,
      FirestoreUserFactory userFactory,
      FirestoreUserReader userReader,
      VipFactory vipFactory,
      VipReader vipReader) {
    super(appConfig, userFactory);
    this.userReader = userReader;
    this.vipFactory = vipFactory;
    this.vipReader = vipReader;
  }

  @Override
  protected List<QueryDocumentSnapshot> getUserList() {
    List<String> channelIds = new ArrayList<>();
    for (QueryDocumentSnapshot snapshot : vipReader.getChannelIds()) {
      Vip vip = vipFactory.createFromSnapshot(snapshot);
      if (vip != null) {
        channelIds.add(vip.getChannelId());
      }
    }
    return userReader.getUserDocumentListByChannelIdList(channelIds);
  }
}
