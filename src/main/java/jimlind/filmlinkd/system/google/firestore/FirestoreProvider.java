package jimlind.filmlinkd.system.google.firestore;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.inject.Inject;
import com.google.inject.Provider;
import jimlind.filmlinkd.config.AppConfig;

/** Provides a singleton instance of the Firestore database client. */
public class FirestoreProvider implements Provider<Firestore> {

  private final Firestore db;

  /**
   * The constructor for this class.
   *
   * @param appConfig Contains application and environment variables
   */
  @Inject
  public FirestoreProvider(AppConfig appConfig) {
    FirestoreOptions.Builder builder =
        FirestoreOptions.getDefaultInstance().toBuilder()
            .setProjectId(appConfig.getGoogleProjectId())
            .setDatabaseId(appConfig.getFirestoreDatabaseId());
    FirestoreOptions firestoreOptions = builder.build();
    db = extractService(firestoreOptions);
  }

  private static Firestore extractService(FirestoreOptions firestoreOptions) {
    return firestoreOptions.getService();
  }

  @Override
  public Firestore get() {
    return db;
  }
}
