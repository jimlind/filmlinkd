package jimlind.filmlinkd.core.string;

import static jimlind.filmlinkd.model.Message.Type.review;

import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/** Utility class for formatting full text of reviews. */
public final class ReviewFormatter {
  private static final int REVIEW_TEXT_MAX_LENGTH = 400;
  private static final char OPENING_HTML_BRACKET = '<';
  private static final char CLOSING_HTML_BRACKET = '>';

  /** Private constructor to prevent instantiation of utility class. */
  private ReviewFormatter() {}

  /**
   * Formats the username for use in Discord, which uses a Markdown-like syntax.
   *
   * @param input The review text as fully formatted HTML string.
   * @return A formatted review in Markdown.
   */
  public static String format(String input) {
    if (input == null) {
      return "";
    }

    // Truncate the review if it's too long.
    boolean truncateText = input.length() > REVIEW_TEXT_MAX_LENGTH;
    String review = input;
    if (truncateText) {
      review = review.substring(0, REVIEW_TEXT_MAX_LENGTH).trim();
    }
    review = truncateClosingHtmlTag(review);

    // Convert from HTML to Markdown
    Document reviewDocument = Jsoup.parseBodyFragment(review);
    Options options = OptionsBuilder.anOptions().withBr("\n").build();
    String markdownReview = new CopyDown(options).convert(reviewDocument.body().toString());

    if (truncateText) {
      markdownReview += "...";
    }

    return markdownReview;
  }

  /**
   * Removes ANY HTML tag (opening or closing) that exists at the end of the input string. A tool
   * like JSoup can easily add any appropriate closing tags to ensure that the HTML is valid later.
   * This method will return invalid HTML.
   *
   * @param input A string that optionally contains HTML.
   * @return The string without any final HTML tags.
   */
  public static String truncateClosingHtmlTag(String input) {
    // Remove the trailing closing tag. This technically creates an invalid HTML string but the
    // parsing later with jsoup will make sure that tags are properly closed.
    String review = input;
    if (input.endsWith(">")) {
      review = input.substring(0, input.length() - 1);
    }

    // Loop over all characters backwards
    for (int i = review.length() - 1; i >= 0; i--) {
      char currentChar = review.charAt(i);
      // If we ever hit a closing bracket now we assume it is a well-formed HTML string
      if (currentChar == CLOSING_HTML_BRACKET) {
        break;
      }
      // If we hit an opening bracket (since we haven't hit a closing bracket above) we assume it is
      // a poorly formatted HTML string and chop it up
      if (currentChar == OPENING_HTML_BRACKET) {
        return review.substring(0, i);
      }
    }

    return review;
  }
}
