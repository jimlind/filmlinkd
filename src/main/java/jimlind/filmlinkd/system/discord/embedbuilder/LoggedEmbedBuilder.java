package jimlind.filmlinkd.system.discord.embedbuilder;

import com.google.inject.Inject;
import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.StarsStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LbReview;
import jimlind.filmlinkd.system.letterboxd.utils.DateUtils;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/** Builds a Discord embed to display information about what a user has logged. */
public class LoggedEmbedBuilder {
  private final DateUtils dateUtils;
  private final EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;
  private List<LbLogEntry> logEntryList = new ArrayList<>();

  /**
   * Constructor for this class.
   *
   * @param dateUtils Helpers for processing Letterboxd dates
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model.
   * @param imageUtils Assists in finding optimal Letterboxd images
   */
  @Inject
  public LoggedEmbedBuilder(
      DateUtils dateUtils, EmbedBuilderFactory embedBuilderFactory, ImageUtils imageUtils) {
    this.dateUtils = dateUtils;
    this.imageUtils = imageUtils;
    embedBuilder = embedBuilderFactory.create();
  }

  /**
   * Setter for the logEntryList attribute.
   *
   * @param logEntryList Log Entry list from Letterboxd API
   * @return This class for chaining
   */
  public LoggedEmbedBuilder setLogEntryList(List<LbLogEntry> logEntryList) {
    this.logEntryList = logEntryList;
    return this;
  }

  /**
   * Builds the embed.
   *
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public ArrayList<MessageEmbed> build() {
    StringBuilder description = new StringBuilder();
    for (LbLogEntry logEntry : logEntryList) {
      String action = "Logged";
      String date = dateUtils.toPattern(logEntry.whenCreated);

      if (logEntry.diaryDetails != null) {
        action = "Watched";
        date = dateUtils.toPattern(logEntry.diaryDetails.diaryDate);
      }

      if (logEntry.review != null) {
        action = "Reviewed";
      }

      description.append(
          String.format("[**%s on %s**](https://boxd.it/%s)\n", action, date, logEntry.id));

      String stars = new StarsStringBuilder().setStarCount(logEntry.rating).build();
      String rewatch =
          logEntry.diaryDetails != null && logEntry.diaryDetails.rewatch
              ? " <:r:851135667546488903>"
              : "";
      String like = logEntry.like ? " <:l:851138401557676073>" : "";
      description.append(stars).append(rewatch).append(like).append("\n");

      if (logEntry.review != null) {
        description.append(formatReview(logEntry.review)).append("\n");
      }
    }

    LbLogEntry firstLogEntry = logEntryList.get(0);
    String title =
        String.format(
            "%s's Recent Entries for %s (%s)\n",
            firstLogEntry.owner.displayName,
            firstLogEntry.film.name,
            firstLogEntry.film.releaseYear);
    embedBuilder.setTitle(title);
    embedBuilder.setThumbnail(imageUtils.getTallest(firstLogEntry.film.poster));
    embedBuilder.setDescription(
        new DescriptionStringBuilder().setDescriptionText(description.toString()).build());

    ArrayList<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }

  private String formatReview(LbReview review) {
    String reviewText = review.text;
    if (reviewText.length() > 200) {
      reviewText = reviewText.substring(0, 200).trim();
    }
    Document reviewDocument = Jsoup.parseBodyFragment(reviewText);
    Options options = OptionsBuilder.anOptions().withBr("\n").build();
    reviewText = new CopyDown(options).convert(reviewDocument.body().toString());
    if (review.text.length() > 200) {
      reviewText += "...";
    }

    reviewText = review.containsSpoilers ? "||" + reviewText + "||" : reviewText;
    reviewText = reviewText.replaceAll("[\r\n]+", "\n");

    return reviewText;
  }
}
