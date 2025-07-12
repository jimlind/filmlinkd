package jimlind.filmlinkd.system.discord.stringbuilder;

public class RunTimeStringBuilder {
  private int runTime = 0;

  public RunTimeStringBuilder setRunTime(int runTime) {
    this.runTime = runTime;
    return this;
  }

  public String build() {
    double hours = Math.floor((double) this.runTime / 60);
    double minutes = this.runTime - hours * 60;

    return String.format("%.0fh %.0fm", hours, minutes);
  }
}
