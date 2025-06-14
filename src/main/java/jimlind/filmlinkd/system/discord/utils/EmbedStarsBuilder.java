package jimlind.filmlinkd.system.discord.utils;

public class EmbedStarsBuilder {
  private final float starCount;

  public EmbedStarsBuilder(float starCount) {
    this.starCount = starCount;
  }

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
