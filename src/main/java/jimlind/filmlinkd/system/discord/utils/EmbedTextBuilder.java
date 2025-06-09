package jimlind.filmlinkd.system.discord.utils;

import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class EmbedTextBuilder {
  private String htmlText;

  public EmbedTextBuilder(String htmlText) {
    this.htmlText = htmlText;
  }

  public String build(int length) {
    if (htmlText.length() > length) {
      htmlText = htmlText.substring(0, length).trim();
    }
    Document document = Jsoup.parseBodyFragment(htmlText);
    Options options = OptionsBuilder.anOptions().withBr("\n").build();
    String markdownText = new CopyDown(options).convert(document.body().toString());
    if (markdownText.length() > length) {
      markdownText += "...";
    }

    return markdownText.replaceAll("[\r\n]+", "\n");
  }
}
