package jimlind.filmlinkd.system.discord.stringbuilder;

import java.util.Locale;

/** Build a string that displays a username escaping characters that might be formatting. */
public class UserStringBuilder {
  private String username = "";

  /**
   * Setter for the username attribute.
   *
   * @param username The raw username string from Letterboxd
   * @return This class for chaining
   */
  public UserStringBuilder setUsername(String username) {
    this.username = username;
    return this;
  }

  /**
   * Builds the string.
   *
   * @return The username suitable for embedding in Discord
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public String build() {
    int position = 0;
    while (true) {
      char firstChar = this.username.charAt(position);
      char lastChar = this.username.charAt(this.username.length() - position - 1);

      if ((firstChar == '_') && firstChar == lastChar) {
        this.username =
            this.username.substring(position + 1, this.username.length() - position - 1);
        position++;
      } else {
        if (position > 0) {
          StringBuilder stringBuilder = new StringBuilder();
          String underscores = "\\_".repeat(position);
          stringBuilder.append(underscores).append(this.username).append(underscores);
          this.username = stringBuilder.toString();
        }
        break;
      }
    }

    return this.username.toLowerCase(Locale.ROOT);
  }
}
