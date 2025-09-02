package jimlind.filmlinkd.system.google.firestore;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import jimlind.filmlinkd.config.AppConfig;

/** Handles all read-only queries for vip data from Firestore. */
public class VipReader {
  private final AppConfig appConfig;
  private final Firestore db;

  /**
   * The constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   * @param firestoreProvider Wrapper for the Firestore database client
   */
  @Inject
  public VipReader(AppConfig appConfig, FirestoreProvider firestoreProvider) {
    this.appConfig = appConfig;
    this.db = firestoreProvider.get();
  }

  /**
   * Gets all VIP documents.
   *
   * @return A list of documents query document snapshots for processing outside the reader
   */
  public List<QueryDocumentSnapshot> getChannelIds() {
    String collectionId = appConfig.getFirestoreVipCollectionId();
    ApiFuture<QuerySnapshot> query = this.db.collection(collectionId).get();

    try {
      return query.get().getDocuments();
    } catch (InterruptedException | ExecutionException e) {
      return Collections.emptyList();
    }
  }
}
