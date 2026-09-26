package jimlind.filmlinkd.database.user;

import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;

/** Handles queries and inserts for user data in the local database. */
@Slf4j
public class UserDao {
  private static final String UPSERT_USER_SQL =
      """
      INSERT INTO users (letterboxd_id, user_name, display_name, image)
      VALUES (:letterboxdId, :userName, :displayName, :image)
      ON CONFLICT (letterboxd_id) DO UPDATE SET
        user_name = EXCLUDED.user_name,
        display_name = EXCLUDED.display_name,
        image = EXCLUDED.image
      """;

  private final Jdbi jdbi;

  /**
   * The constructor for this class.
   *
   * @param jdbi The JDBI database connection
   */
  public UserDao(Jdbi jdbi) {
    this.jdbi = jdbi;
  }

  /**
   * Creates a new user record from the supplied values or updates the existing record when the user
   * already exists.
   *
   * @param userRow The values to store for the user
   * @return true if the record was created or updated; false if an error occurred
   */
  public boolean createUser(UserRow userRow) {
    try {
      jdbi.withHandle(
          handle ->
              handle
                  .createUpdate(UPSERT_USER_SQL)
                  .bind("letterboxdId", userRow.letterboxdId())
                  .bind("userName", userRow.userName())
                  .bind("displayName", userRow.displayName())
                  .bind("image", userRow.image())
                  .execute());
      return true;
    } catch (RuntimeException e) {
      log.atError()
          .setMessage("Unable to upsert user record")
          .addKeyValue("letterboxdId", userRow.letterboxdId())
          .setCause(e)
          .log();
      return false;
    }
  }

  /**
   * Gets the total number of user records in the database.
   *
   * @return The total number of user records
   */
  public long getUserCount() {
    return jdbi.withHandle(
        handle -> handle.createQuery("SELECT count(*) FROM users").mapTo(Long.class).one());
  }
}
