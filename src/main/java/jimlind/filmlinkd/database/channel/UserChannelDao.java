package jimlind.filmlinkd.database.channel;

import org.jdbi.v3.core.Jdbi;

/**
 * Handles queries and inserts for user channel data in the local database.
 *
 * <p>Callers build the values to persist, typically with {@link UserChannelRowFactory}, so this
 * class stays focused on running SQL.
 */
public class UserChannelDao {
  private static final String CREATE_USER_CHANNEL_SQL =
      """
      INSERT INTO user_channels (letterboxd_id, channel_id)
      VALUES (:letterboxdId, :channelId)
      ON CONFLICT (letterboxd_id, channel_id) DO NOTHING
      """;

  private final Jdbi jdbi;

  /**
   * The constructor for this class.
   *
   * @param jdbi The JDBI database connection
   */
  public UserChannelDao(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  /**
   * Creates a new user channel record from the supplied values.
   *
   * @param userChannelRow The values to store for the channel subscription
   * @return true if a new record was created; false if the subscription already exists
   */
  public boolean createUserChannel(UserChannelRow userChannelRow) {
    int rows =
        jdbi.withHandle(
            handle ->
                handle
                    .createUpdate(CREATE_USER_CHANNEL_SQL)
                    .bind("letterboxdId", userChannelRow.letterboxdId())
                    .bind("channelId", userChannelRow.channelId())
                    .execute());

    return rows > 0;
  }
}
