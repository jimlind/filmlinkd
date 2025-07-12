package jimlind.filmlinkd.system.discord.stringbuilder;

import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class TextStringBuilder {
  private String htmlText = "";

  public TextStringBuilder setHtmlText(String htmlText) {
    this.htmlText = htmlText;
    return this;
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
