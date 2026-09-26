package jimlind.filmlinkd.database.user;

import javax.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import jimlind.filmlinkd.system.letterboxd.utils.extractor.MemberAttributes;

/** Builds user rows for the local database from Letterboxd member data. */
public class UserRowFactory {
  private final ImageUtils imageUtils;

  /**
   * The constructor for this class.
   *
   * @param imageUtils Assists in finding optimal Letterboxd images
   */
  @Inject
  public UserRowFactory(ImageUtils imageUtils) {
    this.imageUtils = imageUtils;
  }

  /**
   * Creates the values to store for a user from Letterboxd member data.
   *
   * @param member Letterboxd Member object from API
   * @return Values for the users table
   */
  public UserRow createFromMember(LbMember member) {
    return new UserRow(
        member.getId(),
        MemberAttributes.extractLowercaseUsername(member),
        member.getDisplayName(),
        imageUtils.getTallest(member.getAvatar()));
  }
}
