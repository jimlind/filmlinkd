package jimlind.filmlinkd.google.db;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.List;

/** Handles all read-only queries for vip data. */
public interface VipReaderInterface {
  /**
   * Gets all VIP documents.
   *
   * @return A list of documents query document snapshots for processing outside the reader
   */
  List<QueryDocumentSnapshot> getChannelIds();
}
