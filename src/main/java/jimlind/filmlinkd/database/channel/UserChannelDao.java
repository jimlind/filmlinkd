package jimlind.filmlinkd.database.channel;

import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

/**
 * Handles queries and inserts for user channel data in the local database.
 *
 * <p>Callers build the values to persist, typically with {@link UserChannelRowFactory}, so this
 * class stays focused on running SQL.
 */
@Slf4j
public class UserChannelDao {
  private static final String UPSERT_USER_CHANNEL_SQL =
      """
      INSERT INTO user_channels (letterboxd_id, channel_id)
      VALUES (:letterboxdId, :channelId)
      ON CONFLICT (letterboxd_id, channel_id) DO UPDATE SET
        archived_at = NULL
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
   * Creates a new user channel record from the supplied values or updates the existing record when
   * the subscription already exists. An existing archived subscription is reactivated.
   *
   * @param userChannelRow The values to store for the channel subscription
   * @return true if the record was created or updated; false if an error occurred
   */
  public boolean createUserChannel(UserChannelRow userChannelRow) {
    try {
      jdbi.withHandle(
          handle ->
              handle
                  .createUpdate(UPSERT_USER_CHANNEL_SQL)
                  .bind("letterboxdId", userChannelRow.letterboxdId())
                  .bind("channelId", userChannelRow.channelId())
                  .execute());
      return true;
    } catch (RuntimeException e) {
      log.atError()
          .setMessage("Unable to upsert user channel record")
          .addKeyValue("letterboxdId", userChannelRow.letterboxdId())
          .addKeyValue("channelId", userChannelRow.channelId())
          .setCause(e)
          .log();
      return false;
    }
  }
}
