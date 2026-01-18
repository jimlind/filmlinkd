package jimlind.filmlinkd.google.db;

import com.google.cloud.firestore.Firestore;
import javax.inject.Provider;
import javax.inject.Singleton;

/** Provides a singleton instance of the Firestore database client. */
// TODO: DELETE THIS FILE WHEN I CAN CONFIRM THAT FIRESTOREMODULE WORKS
@Singleton
public class FirestoreProvider implements Provider<Firestore> {
  @Override
  public Firestore get() {
    return null;
  }

  //  private final Firestore db;
  //
  //  /**
  //   * The constructor for this class.
  //   *
  //   * @param appConfig Contains application and environment variables
  //   */
  //  @Inject
  //  public FirestoreProvider(AppConfig appConfig) {
  //    FirestoreOptions.Builder builder =
  //        FirestoreOptions.getDefaultInstance().toBuilder()
  //            .setProjectId(appConfig.getGoogleProjectId())
  //            .setDatabaseId(appConfig.getFirestoreDatabaseId());
  //    FirestoreOptions firestoreOptions = builder.build();
  //    db = extractService(firestoreOptions);
  //  }
  //
  //  private static Firestore extractService(FirestoreOptions firestoreOptions) {
  //    return firestoreOptions.getService();
  //  }
  //
  //  @Override
  //  public Firestore get() {
  //    return db;
  //  }
}
