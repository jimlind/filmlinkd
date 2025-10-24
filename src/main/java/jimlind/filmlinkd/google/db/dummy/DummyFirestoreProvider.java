package jimlind.filmlinkd.google.db.dummy;

import com.google.cloud.firestore.Firestore;
import com.google.inject.Provider;

public class DummyFirestoreProvider implements Provider<Firestore> {
  @Override
  public Firestore get() {
    return null;
  }
}
