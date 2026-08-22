package jimlind.filmlinkd.database;

import org.jdbi.v3.core.Jdbi;

/** Handles all read-only queries for user data from the local database. */
public class UserDao {
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
   * Gets the total number of user records in the database.
   *
   * @return The total number of user records
   */
  public long getUserCount() {
    return jdbi.withHandle(
        handle -> handle.createQuery("SELECT count(*) FROM users").mapTo(Long.class).one());
  }
}
