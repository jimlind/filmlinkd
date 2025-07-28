package jimlind.filmlinkd.system.discord.stringbuilder;

/** Build a string that displays runtime in a user-friendly way. */
public class RuntimeStringBuilder {
  private int runtime;

  /**
   * Setter for the runtime attribute.
   *
   * @param runtime The number of seconds that the film runs
   * @return This class for chaining
   */
  public RuntimeStringBuilder setRuntime(int runtime) {
    this.runtime = runtime;
    return this;
  }

  /**
   * Builds the string.
   *
   * @return The runtime displayed in human-readable format
   */
  public String build() {
    double hours = Math.floor((double) this.runtime / 60);
    double minutes = this.runtime - hours * 60;

    return String.format("%.0fh %.0fm", hours, minutes);
  }
}
