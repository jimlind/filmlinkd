package jimlind.filmlinkd.system.discord.stringbuilder;

/** Build a string that is correctly truncated for a description embed. */
public class DescriptionStringBuilder {
  private String descriptionText = "";

  /**
   * Setter for the description attribute.
   *
   * @param descriptionText The full description text
   * @return This class for chaining
   */
  public DescriptionStringBuilder setDescriptionText(String descriptionText) {
    this.descriptionText = descriptionText;
    return this;
  }

  /**
   * Builds the string.
   *
   * @return The description text potentially truncated
   */
  public String build() {
    int endIndex = Math.min(this.descriptionText.length(), 4096);
    return this.descriptionText.substring(0, endIndex);
  }
}
