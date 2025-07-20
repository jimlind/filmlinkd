package jimlind.filmlinkd.system.discord.stringbuilder;

/** Build a string that displays stars in a user-friendly way. */
public class StarsStringBuilder {
  private float starCount = 0;

  /**
   * Setter for the starCount attribute.
   *
   * @param starCount The number of stars to display. Partials are encouraged.
   * @return This class for chaining
   */
  public StarsStringBuilder setStarCount(float starCount) {
    this.starCount = starCount;
    return this;
  }

  /**
   * Builds the string.
   *
   * @return A string composed of star emotes
   */
  public String build() {
    String fullStar = "<:s:851134022251970610>";
    String halfStar = "<:h:851199023854649374>";

    String starString = fullStar.repeat((int) Math.floor(this.starCount));
    double remainder = this.starCount % 1;
    if (remainder >= 0.75) {
      starString += fullStar;
    } else if (remainder >= 0.25) {
      starString += halfStar;
    }

    return starString;
  }
}
