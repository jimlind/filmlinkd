package jimlind.filmlinkd.factory;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.model.Vip;
import org.jetbrains.annotations.Nullable;

/** A factory for creating instances of the {@link Vip} model. */
@Singleton
public class VipFactory {
  /** Constructor for this class */
  @Inject
  VipFactory() {}

  /**
   * Create {@link User} from a {@link QueryDocumentSnapshot}.
   *
   * @param snapshot A document snapshot from Firestore
   * @return Data model for user information
   */
  public @Nullable Vip createFromSnapshot(@Nullable QueryDocumentSnapshot snapshot) {
    if (snapshot == null) {
      return null;
    }

    return snapshot.toObject(Vip.class);
  }
}
