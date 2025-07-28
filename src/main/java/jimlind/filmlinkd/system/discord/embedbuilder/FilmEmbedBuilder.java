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
import jimlind.filmlinkd.system.letterboxd.model.LbFilmStatisticsCounts;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbGenre;
import jimlind.filmlinkd.system.letterboxd.model.LbLanguage;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display information about a film. */
public class FilmEmbedBuilder {
  private final EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;
  private CombinedLbFilmModel filmCombination;

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
  public List<MessageEmbed> build() {
    if (filmCombination == null) {
      return new ArrayList<>();
    }

    LbFilm film = filmCombination.getFilm();

    String releaseYear =
        film.getReleaseYear() > 0 ? String.format(" (%s)", film.getReleaseYear()) : "";
    embedBuilder.setTitle(film.getName() + releaseYear);
    embedBuilder.setUrl(String.format("https://boxd.it/%s", film.id));
    String imageUrl = imageUtils.getTallest(film.getPoster());
    embedBuilder.setThumbnail(imageUrl.isBlank() ? null : imageUrl);

    StringBuilder descriptionBuilder = new StringBuilder();
    // Add tagline to description
    if (film.tagline != null) {
      descriptionBuilder.append(String.format("**%s**\n", film.tagline));
    }

    // Add rating to description
    LbFilmSummary summary = extractFilmSummary();
    if (summary.rating > 0) {
      String stars = new StarsStringBuilder().setStarCount(summary.rating).build();
      String rating = String.format("%.2f", summary.rating);
      descriptionBuilder.append(stars).append(' ').append(rating).append('\n');
    }

    // Add directors to description
    String directors = new DirectorsStringBuilder().setContributionList(film.contributions).build();
    if (!directors.isBlank()) {
      descriptionBuilder.append(directors).append('\n');
    }

    descriptionBuilder
        .append(createLanguageRuntime(film))
        .append(createGenre(film))
        .append(createStatistics(filmCombination));

    // Build it
    embedBuilder.setDescription(
        new DescriptionStringBuilder().setDescriptionText(descriptionBuilder.toString()).build());
    List<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }

  private String createGenre(LbFilm film) {
    List<LbGenre> genres = extractGenres(film);
    if (!genres.isEmpty()) {
      return genres.stream().map(g -> g.name).collect(Collectors.joining(", ")) + "\n";
    }
    return "";
  }

  private String createLanguageRuntime(LbFilm film) {
    List<String> metadata = new ArrayList<>();
    LbLanguage primaryLanguage = extractPrimaryLanguage(film);
    if (primaryLanguage != null) {
      metadata.add(primaryLanguage.name);
    }
    if (film.runTime > 0) {
      metadata.add(new RuntimeStringBuilder().setRuntime(film.runTime).build());
    }

    String metadataString = "";
    if (!metadata.isEmpty()) {
      metadataString = String.join(", ", metadata) + "\n";
    }

    return metadataString;
  }

  private String createStatistics(CombinedLbFilmModel filmCombination) {
    LbFilmStatisticsCounts counts = filmCombination.getStatisticsCounts();

    return ":eyes: "
        + new CountStringBuilder().setCount(counts.watches).build()
        + ", "
        + "<:r:851138401557676073> "
        + new CountStringBuilder().setCount(counts.likes).build()
        + ", "
        + ":speech_balloon: "
        + new CountStringBuilder().setCount(counts.reviews).build()
        + "\n";
  }

  private LbFilmSummary extractFilmSummary() {
    return filmCombination.filmSummary;
  }

  private List<LbGenre> extractGenres(LbFilm film) {
    return film.genres;
  }

  private LbLanguage extractPrimaryLanguage(LbFilm film) {
    return film.primaryLanguage;
  }
}
