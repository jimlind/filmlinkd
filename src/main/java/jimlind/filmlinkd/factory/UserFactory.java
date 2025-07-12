package jimlind.filmlinkd.factory;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import java.util.ArrayList;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;

/** A factory for creating instances of the {@link User} model. */
public class UserFactory {
  private final ImageUtils imageUtils;

  /**
   * Constructor for the {@link UserFactory}.
   *
   * @param imageUtils Utilities for processing avatar images
   */
  @Inject
  public UserFactory(ImageUtils imageUtils) {
    this.imageUtils = imageUtils;
  }

  /**
   * Create {@link User} from a {@link QueryDocumentSnapshot}.
   *
   * @param snapshot A document snapshot from Firestore
   * @return Data model for user information
   */
  public User createFromSnapshot(QueryDocumentSnapshot snapshot) {
    try {
      User user = snapshot.toObject(User.class);
      return fillDefaults(user);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Create a {@link User} from an {@link LBMember}.
   *
   * @param member A model from the Letterboxd API response
   * @return Data model for user information
   */
  public User createFromMember(LBMember member) {
    try {
      User user = new User();

      user.id = member.id;
      user.created = 0L; // TODO: Should this get filled?
      user.checked = 0L; // TODO: Should this get filled?
      user.displayName = member.displayName;
      user.image = imageUtils.getTallest(member.avatar);
      user.letterboxdId = member.id;
      user.updated = 0L; // TODO: Should this get filled?
      user.userName = member.username.toLowerCase();

      return fillDefaults(user);
    } catch (Exception e) {
      return null;
    }
  }

  /**
   * Fill the User.Previous with enough default data so other systems work as expected. // Maybe
   * there's a better way to do this, but for now this seems reasonable.
   *
   * @param user The user to fill with defaults
   * @return The user now with minimum necessary default data
   */
  private User fillDefaults(User user) {
    //
    if (user.previous == null) {
      user.previous = new User.Previous();
      user.previous.lid = "0";
      user.previous.list = new ArrayList<String>();
    }
    if (user.channelList == null) {
      user.channelList = new ArrayList<User.Channel>();
    }

    return user;
  }
}
