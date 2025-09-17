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

  /** Private constructor to prevent instantiation of utility class. */
  private ReviewFormatter() {}

  /**
   * Formats the username for use in Discord, which uses a Markdown-like syntax.
   *
   * @param review The review text as fully formatted HTML string.
   * @return A formatted review in Markdown.
   */
  public static String format(String review) {
    if (review == null) {
      return "";
    }

    // Truncate the review if it's too long.
    boolean truncateText = review.length() > REVIEW_TEXT_MAX_LENGTH;
    if (truncateText) {
      review = review.substring(0, REVIEW_TEXT_MAX_LENGTH).trim();
    }
    review = truncateClosingHtmlTag(review);

    // Convert from HTML to Markdown
    Document reviewDocument = Jsoup.parseBodyFragment(review);
    Options options = OptionsBuilder.anOptions().withBr("\n").build();
    String markdownReview = new CopyDown(options).convert(reviewDocument.body().toString());

    System.out.println(markdownReview);

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
    if (input.endsWith(">")) {
      input = input.substring(0, input.length() - 1);
    }

    // Loop over all characters backwards
    for (int i = input.length() - 1; i >= 0; i--) {
      char currentChar = input.charAt(i);
      // If we ever hit a closing bracket now we assume it is a well-formed HTML string
      if (currentChar == '>') {
        break;
      }
      // If we hit an opening bracket (since we haven't hit a closing bracket above) we assume it is
      // a poorly formatted HTML string and chop it up
      if (currentChar == '<') {
        return input.substring(0, i);
      }
    }

    return input;
  }
}
