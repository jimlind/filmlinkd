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

@Slf4j
public class DiaryEntryEmbedBuilder {
  private final EmbedBuilder embedBuilder;
  private final StarsStringBuilder starsStringBuilder;
  @Nullable private Message message;
  @Nullable private User user;

  @Inject
  public DiaryEntryEmbedBuilder(
      EmbedBuilderFactory embedBuilderFactory, StarsStringBuilder starsStringBuilder) {
    this.starsStringBuilder = starsStringBuilder;
    embedBuilder = embedBuilderFactory.create();
  }

  public DiaryEntryEmbedBuilder setMessage(Message message) {
    this.message = message;
    return this;
  }

  public DiaryEntryEmbedBuilder setUser(User user) {
    this.user = user;
    return this;
  }

  public ArrayList<MessageEmbed> build() {
    if (message == null || user == null) {
      return new ArrayList<>();
    }

    String profileName = user.displayName;
    String authorTitle = "%s %sed...".formatted(profileName, message.entry.type.toString());
    String profileURL = "https://letterboxd.com/%s/".formatted(user.userName);
    embedBuilder.setAuthor(authorTitle, profileURL, user.image);

    String adult = message.entry.adult ? ":underage: " : "";
    String year = message.entry.filmYear != 0 ? "(" + message.entry.filmYear + ")" : "";
    embedBuilder.setTitle(adult + message.entry.filmTitle + " " + year, message.entry.link);

    // Build the Review Title
    String reviewTitle = "";
    if (message.entry.watchedDate != 0) {
      String pattern =
          Instant.now().toEpochMilli() - message.entry.watchedDate < 5000000000L
              ? "**MMM dd**"
              : "**MMM dd uuu**";
      reviewTitle =
          LocalDateTime.ofEpochSecond(message.entry.watchedDate / 1000, 0, ZoneOffset.UTC)
              .format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }

    if (message.entry.starCount > 0) {
      reviewTitle += starsStringBuilder.setStarCount(message.entry.starCount).build();
    }
    if (message.entry.rewatch != null && message.entry.rewatch) {
      reviewTitle += " <:r:851135667546488903>";
    }
    if (message.entry.liked != null && message.entry.liked) {
      reviewTitle += " <:l:851138401557676073>";
    }
    reviewTitle = !reviewTitle.isEmpty() ? reviewTitle + "\u200b\n" : "";

    // Build the Review Text
    String reviewText = message.entry.review;
    if (message.entry.review.length() > 400) {
      reviewText = reviewText.substring(0, 400).trim();
    }
    Document reviewDocument = Jsoup.parseBodyFragment(reviewText);
    Options options = OptionsBuilder.anOptions().withBr("\n").build();
    reviewText = new CopyDown(options).convert(reviewDocument.body().toString());
    if (message.entry.review.length() > 400) {
      reviewText += "...";
    }

    // Format Review Title and Review Text as EmbedDescription
    reviewText = message.entry.containsSpoilers ? "||" + reviewText + "||" : reviewText;
    reviewText = reviewText.replaceAll("[\r\n]+", "\n");
    String rule = reviewTitle.length() > 1 && reviewText.length() > 1 ? "┈".repeat(12) + "\n" : "";
    embedBuilder.setDescription(reviewTitle + rule + reviewText);

    // If there is footer data with actual data then include it.
    if (user.footer != null && !user.footer.text.isBlank()) {
      embedBuilder.setFooter(user.footer.text, user.footer.icon);
    }

    // If there is an image then include it
    if (!message.entry.image.isBlank()) {
      embedBuilder.setThumbnail(message.entry.image);
    }

    ArrayList<MessageEmbed> collection = new ArrayList<MessageEmbed>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
