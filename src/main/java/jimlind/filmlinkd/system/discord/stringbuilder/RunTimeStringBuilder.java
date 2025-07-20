package jimlind.filmlinkd.system.discord.stringbuilder;

/** Build a string that displays runtime in a user-friendly way. */
public class RunTimeStringBuilder {
  private int runTime = 0;

  /**
   * Setter for the runTime attribute.
   *
   * @param runTime The number of seconds that the film runs
   * @return This class for chaining
   */
  public RunTimeStringBuilder setRunTime(int runTime) {
    this.runTime = runTime;
    return this;
  }

  /**
   * Builds the string.
   *
   * @return The runtime displayed in human-readable format
   */
  public String build() {
    double hours = Math.floor((double) this.runTime / 60);
    double minutes = this.runTime - hours * 60;

    return String.format("%.0fh %.0fm", hours, minutes);
  }
}
