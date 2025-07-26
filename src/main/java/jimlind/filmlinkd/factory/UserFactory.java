package jimlind.filmlinkd.factory;

import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Locale;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
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
    User user = snapshot.toObject(User.class);
    return fillDefaults(user);
  }

  /**
   * Create a {@link User} from an {@link LbMember}.
   *
   * @param member A model from the Letterboxd API response
   * @return Data model for user information
   */
  public User createFromMember(LbMember member) {
    User user = new User();

    user.setId(member.id);
    user.setCreated(0L); // TODO: Should this get filled?
    user.setChecked(0L); // TODO: Should this get filled?
    user.setDisplayName(member.displayName);
    user.setImage(imageUtils.getTallest(member.avatar));
    user.setLetterboxdId(member.id);
    user.setUpdated(0L); // TODO: Should this get filled?
    user.setUserName(member.username.toLowerCase(Locale.ROOT));

    return fillDefaults(user);
  }

  /**
   * Fill the User.Previous with enough default data so other systems work as expected. // Maybe
   * there's a better way to do this, but for now this seems reasonable.
   *
   * @param user The user to fill with defaults
   * @return The user now with minimum necessary default data
   */
  private User fillDefaults(User user) {
    // If previous data isn't set in user model set some defaults
    if (user.previous == null) {
      User.Previous previous = new User.Previous();
      previous.setLid("0");
      previous.setList(new ArrayList<>());
      user.setPrevious(previous);
    }

    // If channelList data isn't set in user model set some defaults
    if (user.channelList == null) {
      user.setChannelList(new ArrayList<>());
    }

    return user;
  }
}
