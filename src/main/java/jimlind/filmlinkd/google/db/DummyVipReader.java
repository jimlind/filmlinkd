package jimlind.filmlinkd.google.db;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.List;

/** Handles all read-only queries for vip data from Firestore. */
public class DummyVipReader implements VipReaderInterface {
  /**
   * Always returns an empty list.
   *
   * @return Always empty list
   */
  public List<QueryDocumentSnapshot> getChannelIds() {
    return List.of();
  }
}
