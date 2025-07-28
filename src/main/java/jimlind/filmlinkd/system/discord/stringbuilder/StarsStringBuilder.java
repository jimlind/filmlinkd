package jimlind.filmlinkd.system.discord.stringbuilder;

/** Build a string that displays stars in a user-friendly way. */
public class StarsStringBuilder {
  private static final double REMAINDER_THRESHOLD_FULL_STAR = 0.75;
  private static final double REMAINDER_THRESHOLD_HALF_STAR = 0.25;
  private float starCount;

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

    StringBuilder starStringBuilder = new StringBuilder();
    starStringBuilder.append(fullStar.repeat((int) Math.floor(this.starCount)));
    double remainder = this.starCount % 1;
    if (remainder >= REMAINDER_THRESHOLD_FULL_STAR) {
      starStringBuilder.append(fullStar);
    } else if (remainder >= REMAINDER_THRESHOLD_HALF_STAR) {
      starStringBuilder.append(halfStar);
    }

    return starStringBuilder.toString();
  }
}
