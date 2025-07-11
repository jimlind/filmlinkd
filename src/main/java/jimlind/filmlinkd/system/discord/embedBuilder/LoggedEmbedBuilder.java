package jimlind.filmlinkd.system.discord.embedBuilder;

import com.google.inject.Inject;
import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringBuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringBuilder.StarsStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBReview;
import jimlind.filmlinkd.system.letterboxd.utils.DateUtils;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class LoggedEmbedBuilder {
  private final DateUtils dateUtils;
  private final EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;
  private List<LBLogEntry> logEntryList = new ArrayList<>();

  @Inject
  public LoggedEmbedBuilder(
      DateUtils dateUtils, EmbedBuilderFactory embedBuilderFactory, ImageUtils imageUtils) {
    this.dateUtils = dateUtils;
    this.imageUtils = imageUtils;
    embedBuilder = embedBuilderFactory.create();
  }

  public LoggedEmbedBuilder setLogEntryList(List<LBLogEntry> logEntryList) {
    this.logEntryList = logEntryList;
    return this;
  }

  public ArrayList<MessageEmbed> build() {
    StringBuilder description = new StringBuilder();
    for (LBLogEntry logEntry : logEntryList) {
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

    LBLogEntry firstLogEntry = logEntryList.get(0);
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

  private String formatReview(LBReview review) {
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
