package jimlind.filmlinkd.scraper.cache;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.config.AppConfig;
import jimlind.filmlinkd.factory.UserFactory;
import jimlind.filmlinkd.factory.VipFactory;
import jimlind.filmlinkd.google.db.UserReader;
import jimlind.filmlinkd.google.db.VipReader;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.model.Vip;

/** The VIP User Cache contains all users in VIP servers. */
@Singleton
public class VipUserCache extends BaseUserCache {
  private final UserReader userReader;
  private final VipFactory vipFactory;
  private final VipReader vipReader;

  /**
   * Constructor for this class.
   *
   * @param appConfig Holds all the configs that determine how the system run
   * @param userFactory Factory for creating {@link User} model
   * @param userReader Handles all read-only queries for user data from Firestore
   * @param vipFactory Factory for creating {@link Vip} model
   * @param vipReader Handles all read-only queries for VIP data from Firestore
   */
  @Inject
  public VipUserCache(
      AppConfig appConfig,
      UserFactory userFactory,
      UserReader userReader,
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
