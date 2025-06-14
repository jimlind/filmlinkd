package jimlind.filmlinkd.system.discord.utils;

public class EmbedRunTimeBuilder {
  private final int runTime;

  public EmbedRunTimeBuilder(int runTime) {
    this.runTime = runTime;
  }

  public String build() {
    double hours = Math.floor((double) this.runTime / 60);
    double minutes = this.runTime - hours * 60;

    return String.format("%.0fh %.0fm", hours, minutes);
  }
}
