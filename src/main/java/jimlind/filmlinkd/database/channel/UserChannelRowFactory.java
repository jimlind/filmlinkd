package jimlind.filmlinkd.database.channel;

import javax.inject.Inject;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;

/** Builds user channel rows for the local database. */
public class UserChannelRowFactory {
  /** Constructor for this class. */
  @Inject
  public UserChannelRowFactory() {}

  /**
   * Creates the values to store for a channel subscription.
   *
   * @param member Letterboxd Member object from API
   * @param channelId Discord Channel id as a string
   * @return Values for the user_channels table
   */
  public UserChannelRow createFromMember(LbMember member, String channelId) {
    return new UserChannelRow(member.getId(), Long.parseLong(channelId));
  }
}
