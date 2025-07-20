package jimlind.filmlinkd.system.discord.embedbuilder;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.model.CombinedLbFilmModel;
import jimlind.filmlinkd.system.discord.stringbuilder.CountStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.DirectorsStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.RuntimeStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.StarsStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LbFilm;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmStatisticsCounts;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display information about a film. */
public class FilmEmbedBuilder {
  private final EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;
  private CombinedLbFilmModel filmCombination = null;

  /**
   * Constructor for this class.
   *
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model
   * @param imageUtils Assists in finding optimal Letterboxd images
   */
  @Inject
  FilmEmbedBuilder(EmbedBuilderFactory embedBuilderFactory, ImageUtils imageUtils) {
    embedBuilder = embedBuilderFactory.create();
    this.imageUtils = imageUtils;
  }

  /**
   * Setter for the filmCombination attribute.
   *
   * @param filmCombination Film model created from multiple Letterboxd API calls
   * @return This class for chaining
   */
  public FilmEmbedBuilder setFilmCombination(CombinedLbFilmModel filmCombination) {
    this.filmCombination = filmCombination;
    return this;
  }

  /**
   * Builds the embed.
   *
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public ArrayList<MessageEmbed> build() {
    if (filmCombination == null) {
      return new ArrayList<>();
    }

    LbFilm film = filmCombination.film;
    String releaseYear = film.releaseYear > 0 ? String.format(" (%s)", film.releaseYear) : "";
    String imageUrl = imageUtils.getTallest(film.poster);

    embedBuilder.setTitle(film.name + releaseYear);
    embedBuilder.setUrl(String.format("https://boxd.it/%s", film.id));
    embedBuilder.setThumbnail(imageUrl.isBlank() ? null : imageUrl);

    String description = "";
    // Add tagline to description
    if (film.tagline != null) {
      description += String.format("**%s**\n", film.tagline);
    }

    // Add rating to description
    LbFilmSummary summary = filmCombination.filmSummary;
    if (summary.rating > 0) {
      String stars = new StarsStringBuilder().setStarCount(summary.rating).build();
      String rating = String.format("%.2f", summary.rating);
      description += stars + " " + rating + "\n";
    }

    // Add directors to description
    String directors = new DirectorsStringBuilder().setContributionList(film.contributions).build();
    if (!directors.isBlank()) {
      description += directors + "\n";
    }

    // Add primary language and runtime
    List<String> metadata = new ArrayList<>();
    if (film.primaryLanguage != null) {
      metadata.add(film.primaryLanguage.name);
    }
    if (film.runTime > 0) {
      metadata.add(new RuntimeStringBuilder().setRuntime(film.runTime).build());
    }
    if (!metadata.isEmpty()) {
      description += String.join(", ", metadata) + "\n";
    }

    // Add genres
    if (!film.genres.isEmpty()) {
      String genres = film.genres.stream().map(g -> g.name).collect(Collectors.joining(", "));
      description += genres + "\n";
    }

    // Add statistics counts
    LbFilmStatistics statistics = filmCombination.filmStatistics;
    LbFilmStatisticsCounts counts = statistics.counts;
    description += ":eyes: " + new CountStringBuilder().setCount(counts.watches).build() + ", ";
    description +=
        "<:r:851138401557676073> " + new CountStringBuilder().setCount(counts.likes).build() + ", ";
    description +=
        ":speech_balloon: " + new CountStringBuilder().setCount(counts.reviews).build() + "\n";

    // Build it
    embedBuilder.setDescription(
        new DescriptionStringBuilder().setDescriptionText(description).build());
    ArrayList<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }
}
