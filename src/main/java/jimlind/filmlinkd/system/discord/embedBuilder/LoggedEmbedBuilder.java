package jimlind.filmlinkd.system.discord.embedBuilder;

import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.system.discord.utils.EmbedBuilder;
import jimlind.filmlinkd.system.discord.utils.EmbedDescriptionBuilder;
import jimlind.filmlinkd.system.discord.utils.EmbedStarsBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBReview;
import jimlind.filmlinkd.system.letterboxd.utils.DateUtils;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class LoggedEmbedBuilder {

  public ArrayList<MessageEmbed> build(List<LBLogEntry> logEntryList) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

    StringBuilder description = new StringBuilder();
    for (LBLogEntry logEntry : logEntryList) {
      String action = "Logged";
      String date = DateUtils.toPattern(logEntry.whenCreated);

      if (logEntry.diaryDetails != null) {
        action = "Watched";
        date = DateUtils.toPattern(logEntry.diaryDetails.diaryDate);
      }

      if (logEntry.review != null) {
        action = "Reviewed";
      }

      description.append(
          String.format("[**%s on %s**](https://boxd.it/%s)\n", action, date, logEntry.id));

      String stars = new EmbedStarsBuilder(logEntry.rating).build();
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
    embedBuilder.setThumbnail(ImageUtils.getTallest(firstLogEntry.film.poster));
    embedBuilder.setDescription(new EmbedDescriptionBuilder(description.toString()).build());

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
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
