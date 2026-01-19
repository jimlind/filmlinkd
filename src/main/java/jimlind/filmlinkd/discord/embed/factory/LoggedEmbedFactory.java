package jimlind.filmlinkd.discord.embed.factory;

import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.StarsStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LbDiaryDetails;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbReview;
import jimlind.filmlinkd.system.letterboxd.utils.DateUtils;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/** Builds a Discord embed to display information about what a user has logged. */
public class LoggedEmbedFactory {
  private static final int MAX_REVIEW_LENGTH = 200;
  private final EmbedBuilderFactory embedBuilderFactory;
  private final ImageUtils imageUtils;

  /**
   * Constructor for this class.
   *
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model.
   * @param imageUtils Assists in finding optimal Letterboxd images
   */
  @Inject
  public LoggedEmbedFactory(EmbedBuilderFactory embedBuilderFactory, ImageUtils imageUtils) {
    this.embedBuilderFactory = embedBuilderFactory;
    this.imageUtils = imageUtils;
  }

  @Nullable
  private static LbDiaryDetails getDiaryDetails(LbLogEntry logEntry) {
    return logEntry.getDiaryDetails();
  }

  private static LbFilmSummary getFilm(LbLogEntry firstLogEntry) {
    return firstLogEntry.getFilm();
  }

  private static LbMemberSummary getOwner(LbLogEntry firstLogEntry) {
    return firstLogEntry.getOwner();
  }

  /**
   * Builds the embed.
   *
   * @param logEntryList Log Entry list from Letterboxd API
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public List<MessageEmbed> create(List<LbLogEntry> logEntryList) {
    EmbedBuilder embedBuilder = embedBuilderFactory.create();
    StringBuilder description = new StringBuilder();
    for (LbLogEntry logEntry : logEntryList) {
      String action = "Logged";
      String date = DateUtils.toPattern(logEntry.whenCreated);

      LbDiaryDetails diaryDetails = getDiaryDetails(logEntry);
      if (diaryDetails != null) {
        action = "Watched";
        date = DateUtils.toPattern(diaryDetails.diaryDate);
      }

      if (logEntry.review != null) {
        action = "Reviewed";
      }

      description.append(
          String.format("[**%s on %s**](https://boxd.it/%s)\n", action, date, logEntry.id));

      String stars = new StarsStringBuilder().setStarCount(logEntry.rating).build();
      String rewatch =
          diaryDetails != null && diaryDetails.isRewatch() ? " <:r:851135667546488903>" : "";
      String like = logEntry.isLike() ? " <:l:851138401557676073>" : "";
      description.append(stars).append(rewatch).append(like).append('\n');

      if (logEntry.review != null) {
        description.append(formatReview(logEntry.review)).append('\n');
      }
    }

    LbLogEntry firstLogEntry = logEntryList.getFirst();
    String title =
        String.format(
            "%s's Recent Entries for %s (%s)\n",
            getOwner(firstLogEntry).displayName,
            getFilm(firstLogEntry).name,
            getFilm(firstLogEntry).releaseYear);
    embedBuilder.setTitle(title);
    embedBuilder.setThumbnail(imageUtils.getTallest(getFilm(firstLogEntry).poster));
    embedBuilder.setDescription(
        new DescriptionStringBuilder().setDescriptionText(description.toString()).build());

    List<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }

  private String formatReview(LbReview review) {
    String reviewText = review.getText();
    if (reviewText.length() > MAX_REVIEW_LENGTH) {
      reviewText = reviewText.substring(0, MAX_REVIEW_LENGTH).trim();
    }
    Document reviewDocument = Jsoup.parseBodyFragment(reviewText);
    Options options = OptionsBuilder.anOptions().withBr("\n").build();
    reviewText = new CopyDown(options).convert(reviewDocument.body().toString());
    if (review.getText().length() > MAX_REVIEW_LENGTH) {
      reviewText += "...";
    }

    reviewText = review.isContainsSpoilers() ? "||" + reviewText + "||" : reviewText;
    reviewText = reviewText.replaceAll("[\r\n]+", "\n");

    return reviewText;
  }
}
