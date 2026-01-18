package jimlind.filmlinkd.core.di;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;
import jimlind.filmlinkd.config.AppConfig;

@Module
public class FirestoreModule {
  private static Firestore extractService(FirestoreOptions firestoreOptions) {
    return firestoreOptions.getService();
  }

  @Provides
  @Singleton
  public Firestore provideFirestore(AppConfig appConfig) {
    FirestoreOptions.Builder builder =
        FirestoreOptions.getDefaultInstance().toBuilder()
            .setProjectId(appConfig.getGoogleProjectId())
            .setDatabaseId(appConfig.getFirestoreDatabaseId());
    FirestoreOptions firestoreOptions = builder.build();
    return extractService(firestoreOptions);
  }
}
