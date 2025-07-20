package jimlind.filmlinkd.system.discord.stringbuilder;

/** Build a string that displays a username escaping characters that might be formatting. */
public class UserStringBuilder {
  private String userName = "";

  /**
   * Setter for the userName attribute.
   *
   * @param userName The raw user name string from Letterboxd
   * @return This class for chaining
   */
  public UserStringBuilder setUserName(String userName) {
    this.userName = userName;
    return this;
  }

  /**
   * Builds the string.
   *
   * @return The username suitable for embedding in Discord
   */
  public String build() {
    int position = 0;
    while (true) {
      char firstChar = this.userName.charAt(position);
      char lastChar = this.userName.charAt(this.userName.length() - position - 1);

      if ((firstChar == '_') && firstChar == lastChar) {
        this.userName =
            this.userName.substring(position + 1, this.userName.length() - position - 1);
        position++;
      } else {
        if (position > 0) {
          String underscores = "\\_".repeat(position);
          this.userName = underscores + this.userName + underscores;
        }
        break;
      }
    }

    return this.userName.toLowerCase();
  }
}
