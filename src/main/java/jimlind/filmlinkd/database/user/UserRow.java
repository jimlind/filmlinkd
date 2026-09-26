package jimlind.filmlinkd.database.user;

/** Values stored in the users table for a single user. */
public record UserRow(String letterboxdId, String userName, String displayName, String image) {}
