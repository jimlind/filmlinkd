package jimlind.filmlinkd.system.discord.stringbuilder;

import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/** Build a Markdown text string based on input of another format. */
public class TextStringBuilder {
  private String htmlText = "";

  /**
   * Setter for the htmlText attribute.
   *
   * @param htmlText A string in HTML format
   * @return This class for chaining
   */
  public TextStringBuilder setHtmlText(String htmlText) {
    this.htmlText = htmlText;
    return this;
  }

  /**
   * Builds the string. The length is used to truncate the HTML string before translating to
   * Markdown. This was done because tooling makes it easy to parse partial HTML and translate.
   *
   * @param length The maximum length of the input string
   * @return The string with Markdown formatting
   */
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
