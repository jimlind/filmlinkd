package jimlind.filmlinkd.system.discord.embedbuilder;

import com.google.inject.Inject;
import io.github.furstenheim.CopyDown;
import io.github.furstenheim.Options;
import io.github.furstenheim.OptionsBuilder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.discord.stringbuilder.StarsStringBuilder;
import lombok.extern.slf4j.Slf4j;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.Nullable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/** Builds a Discord embed to display information about a specific diary entry. */
@Slf4j
public class DiaryEntryEmbedBuilder {
  private static final int REVIEW_TEXT_MAX_LENGTH = 400;
  private final EmbedBuilder embedBuilder;
  private final StarsStringBuilder starsStringBuilder;
  @Nullable private Message message;
  @Nullable private User user;

  /**
   * Constructor for this class.
   *
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model
   * @param starsStringBuilder Builds the stars emoji string
   */
  @Inject
  public DiaryEntryEmbedBuilder(
      EmbedBuilderFactory embedBuilderFactory, StarsStringBuilder starsStringBuilder) {
    this.starsStringBuilder = starsStringBuilder;
    embedBuilder = embedBuilderFactory.create();
  }

  /**
   * Setter for the message attribute.
   *
   * @param message Message model here contains the data for a new diary entry usually retrieved
   *     from the PubSub listener
   * @return This class for chaining
   */
  public DiaryEntryEmbedBuilder setMessage(Message message) {
    this.message = message;
    return this;
  }

  /**
   * Setter for the user attribute.
   *
   * @param user User model containing important data to display about the user
   * @return This class for chaining
   */
  public DiaryEntryEmbedBuilder setUser(User user) {
    this.user = user;
    return this;
  }

  /**
   * Builds the embed.
   *
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public List<MessageEmbed> build() {
    if (message == null || user == null) {
      return new ArrayList<>();
    }

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
    String reviewText = entry.getReview();
    if (reviewText.length() > REVIEW_TEXT_MAX_LENGTH) {
      reviewText = reviewText.substring(0, REVIEW_TEXT_MAX_LENGTH).trim();
    }
    Document reviewDocument = Jsoup.parseBodyFragment(reviewText);
    Options options = OptionsBuilder.anOptions().withBr("\n").build();
    reviewText = new CopyDown(options).convert(reviewDocument.body().toString());
    if (entry.getReview().length() > REVIEW_TEXT_MAX_LENGTH) {
      reviewText += "...";
    }

    // Format Review Title and Review Text as EmbedDescription
    reviewText = entry.isContainsSpoilers() ? "||" + reviewText + "||" : reviewText;
    reviewText = reviewText.replaceAll("[\r\n]+", "\n");

    return reviewText;
  }

  private String extractImage(Message.Entry entry) {
    return entry.getImage();
  }

  private String extractLink(Message.Entry entry) {
    return entry.getLink();
  }
}
