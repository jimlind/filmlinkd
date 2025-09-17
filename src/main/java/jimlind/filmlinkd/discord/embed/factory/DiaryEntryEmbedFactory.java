package jimlind.filmlinkd.discord.embed.factory;

import com.google.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jimlind.filmlinkd.core.string.ReviewFormatter;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.discord.stringbuilder.StarsStringBuilder;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display information about a specific diary entry. */
@Slf4j
public class DiaryEntryEmbedFactory {
  private static final int REVIEW_TEXT_MAX_LENGTH = 400;
  private final EmbedBuilderFactory embedBuilderFactory;
  private final StarsStringBuilder starsStringBuilder;

  /**
   * Constructor for this class.
   *
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model
   * @param starsStringBuilder Builds the stars emoji string
   */
  @Inject
  public DiaryEntryEmbedFactory(
      EmbedBuilderFactory embedBuilderFactory, StarsStringBuilder starsStringBuilder) {
    this.embedBuilderFactory = embedBuilderFactory;
    this.starsStringBuilder = starsStringBuilder;
  }

  private static String extractImage(Message.Entry entry) {
    return entry.getImage();
  }

  private static String extractLink(Message.Entry entry) {
    return entry.getLink();
  }

  /**
   * Builds the embed.
   *
   * @param message Message model here contains the data for a new diary entry usually retrieved
   *     from the PubSub listener
   * @param user User model containing important data to display about the user
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public List<MessageEmbed> create(Message message, User user) {
    try {
      return createInternal(message, user);
    } catch (IllegalArgumentException e) {
      log.atWarn()
          .setMessage("Diary Entry Embed build failed")
          .addKeyValue("message", message)
          .addKeyValue("user", user)
          .setCause(e)
          .log();
    }
    return new ArrayList<>();
  }

  private List<MessageEmbed> createInternal(Message message, User user) {
    EmbedBuilder embedBuilder = embedBuilderFactory.create();

    Message.Entry entry = message.getEntry();
    String authorTitle = createAuthorTitle(entry, user);
    String profileUrl = "https://letterboxd.com/%s/".formatted(user.getUserName());
    embedBuilder.setAuthor(authorTitle, profileUrl, user.getImage());

    String footerText = user.getFooterText();
    String footerIcon = user.getFooterIcon();
    if (!footerText.isBlank() && !footerIcon.isBlank()) {
      embedBuilder.setFooter(footerText, footerIcon);
    }

    String embedTitle = createEmbedTitle(entry);
    embedBuilder.setTitle(embedTitle, extractLink(entry));

    String reviewTitle = createReviewTitle(entry);
    String reviewText = createReviewText(entry);
    String rule = reviewTitle.length() > 1 && reviewText.length() > 1 ? "┈".repeat(12) + "\n" : "";
    embedBuilder.setDescription(reviewTitle + rule + reviewText);

    // If there is an image then include it
    String image = extractImage(entry);
    if (!image.isBlank()) {
      embedBuilder.setThumbnail(image);
    }

    List<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }

  private String createAuthorTitle(Message.Entry entry, User user) {
    String profileName = user.getDisplayName();
    return "%s %sed...".formatted(profileName, String.valueOf(entry.getType()));
  }

  private String createEmbedTitle(Message.Entry entry) {
    String adult = entry.isAdult() ? ":underage: " : "";
    String year = entry.getFilmYear() != 0 ? "(" + entry.getFilmYear() + ")" : "";

    return adult + entry.getFilmTitle() + " " + year;
  }

  private String createReviewTitle(Message.Entry entry) {
    StringBuilder reviewTitleBuilder = new StringBuilder(48);
    if (entry.getWatchedDate() != 0) {
      String pattern =
          Instant.now().toEpochMilli() - entry.getWatchedDate() < 5000000000L
              ? "**MMM dd**"
              : "**MMM dd uuu**";
      reviewTitleBuilder.append(
          LocalDateTime.ofEpochSecond(entry.getWatchedDate() / 1000, 0, ZoneOffset.UTC)
              .format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH)));
    }

    if (entry.getStarCount() > 0) {
      reviewTitleBuilder.append(starsStringBuilder.setStarCount(entry.getStarCount()).build());
    }
    if (entry.isRewatch()) {
      reviewTitleBuilder.append(" <:r:851135667546488903>");
    }
    if (entry.isLiked()) {
      reviewTitleBuilder.append(" <:l:851138401557676073>");
    }
    String reviewTitle = reviewTitleBuilder.toString();
    return !reviewTitle.isEmpty() ? reviewTitle + "\u200b\n" : "";
  }

  private String createReviewText(Message.Entry entry) {
    String reviewText = ReviewFormatter.format(entry.getReview());

    // Add spoiler formatting if necessary
    reviewText = entry.isContainsSpoilers() ? "||" + reviewText + "||" : reviewText;
    // Reduce multiple newline characters to single newline
    reviewText = reviewText.replaceAll("[\r\n]+", "\n");

    return reviewText;
  }
}
