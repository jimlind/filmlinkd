package jimlind.filmlinkd.factory;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.model.FirestoreUser;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import jimlind.filmlinkd.system.letterboxd.utils.extractor.MemberAttributes;
import org.jetbrains.annotations.Nullable;

/** A factory for creating instances of the {@link FirestoreUser} model. */
@Singleton
public class FirestoreUserFactory {
  private final ImageUtils imageUtils;

  /**
   * Constructor for the {@link FirestoreUserFactory}.
   *
   * @param imageUtils Utilities for processing avatar images
   */
  @Inject
  FirestoreUserFactory(ImageUtils imageUtils) {
    this.imageUtils = imageUtils;
  }

  /**
   * Create {@link FirestoreUser} from a {@link QueryDocumentSnapshot}.
   *
   * @param snapshot A document snapshot from Firestore
   * @return Data model for user information
   */
  public @Nullable FirestoreUser createFromSnapshot(@Nullable QueryDocumentSnapshot snapshot) {
    if (snapshot == null) {
      return null;
    }

    FirestoreUser user = snapshot.toObject(FirestoreUser.class);
    return fillDefaults(user);
  }

  /**
   * Create a {@link FirestoreUser} from an {@link LbMember}.
   *
   * @param member A model from the Letterboxd API response
   * @return Data model for user information
   */
  public FirestoreUser createFromMember(LbMember member) {
    FirestoreUser user = new FirestoreUser();

    user.setId(member.id);
    user.setCreated(0L);
    user.setChecked(0L);
    user.setDisplayName(member.displayName);
    user.setImage(imageUtils.getTallest(member.avatar));
    user.setLetterboxdId(member.id);
    user.setUpdated(0L);
    user.setUserName(MemberAttributes.extractLowercaseUsername(member));

    return fillDefaults(user);
  }

  /**
   * Fill the FirestoreUser.Previous with enough default data so other systems work as expected. //
   * Maybe there's a better way to do this, but for now this seems reasonable.
   *
   * @param user The user to fill with defaults
   * @return The user now with minimum necessary default data
   */
  private FirestoreUser fillDefaults(FirestoreUser user) {
    // If previous data isn't set in user model set some defaults
    if (user.previous == null) {
      FirestoreUser.Previous previous = new FirestoreUser.Previous();
      previous.setLid("0");
      previous.setList(new ArrayList<>());
      user.setPrevious(previous);
    }

    // If channelList data isn't set in user model set some defaults
    if (user.channelList == null) {
      user.setChannelList(new ArrayList<>());
    }

    // If archivedChannelList data isn't set in user model set some defaults
    if (user.archivedChannelList == null) {
      user.setArchivedChannelList(new ArrayList<>());
    }

    return user;
  }
}
