package jimlind.filmlinkd.system.discord.utils;

public class EmbedDescriptionBuilder {
  private String descriptionText = "";

  public EmbedDescriptionBuilder(String descriptionText) {
    this.descriptionText = descriptionText;
  }

  public String build() {
    int endIndex = Math.min(this.descriptionText.length(), 4096);
    return this.descriptionText.substring(0, endIndex);
  }
}
