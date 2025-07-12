package jimlind.filmlinkd.system.discord.stringbuilder;

public class DescriptionStringBuilder {
  private String descriptionText = "";

  public DescriptionStringBuilder setDescriptionText(String descriptionText) {
    this.descriptionText = descriptionText;
    return this;
  }

  public String build() {
    int endIndex = Math.min(this.descriptionText.length(), 4096);
    return this.descriptionText.substring(0, endIndex);
  }
}
