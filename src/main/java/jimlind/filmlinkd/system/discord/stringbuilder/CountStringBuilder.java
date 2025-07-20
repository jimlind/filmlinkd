package jimlind.filmlinkd.system.discord.stringbuilder;

/** Build a string that displays a count in a user-friendly way. */
public class CountStringBuilder {
  private long count = 0L;

  /**
   * Setter for the count attribute.
   *
   * @param count The quantity of things
   * @return This class for chaining
   */
  public CountStringBuilder setCount(long count) {
    this.count = count;
    return this;
  }

  /**
   * Builds the string.
   *
   * @return A user-friendly count display
   */
  public String build() {
    long thousand = 1000L;
    long million = 1000000L;
    long billion = 1000000000L;

    String suffix = "";
    float number = this.count;

    if (this.count >= billion) {
      suffix = "B";
      number = (float) this.count / billion;
    } else if (this.count >= million) {
      suffix = "M";
      number = (float) this.count / million;
    } else if (this.count >= thousand) {
      suffix = "K";
      number = (float) this.count / thousand;
    }

    return this.format(number) + suffix;
  }

  private String format(double input) {
    double rounded = (double) Math.round(input * 10) / 10;
    if (rounded == (long) rounded) {
      return String.format("%d", (long) rounded);
    } else {
      return String.format("%.1f", rounded);
    }
  }
}
