package jimlind.filmlinkd.database.user;

import org.jdbi.v3.core.Jdbi;

/** Handles queries and inserts for user data in the local database. */
public class UserDao {
  private static final String CREATE_USER_SQL =
      """
      INSERT INTO users (letterboxd_id, user_name, display_name, image)
      VALUES (:letterboxdId, :userName, :displayName, :image)
      ON CONFLICT (letterboxd_id) DO NOTHING
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
   * Creates a new user record from the supplied values.
   *
   * @param userRow The values to store for the user
   * @return true if a new user record was created; false if the user already exists
   */
  public boolean createUser(UserRow userRow) {
    int rows =
        jdbi.withHandle(
            handle ->
                handle
                    .createUpdate(CREATE_USER_SQL)
                    .bind("letterboxdId", userRow.letterboxdId())
                    .bind("userName", userRow.userName())
                    .bind("displayName", userRow.displayName())
                    .bind("image", userRow.image())
                    .execute());

    return rows > 0;
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
