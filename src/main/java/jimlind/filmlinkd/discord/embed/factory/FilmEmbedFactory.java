package jimlind.filmlinkd.discord.embed.factory;

import com.google.inject.Inject;
import java.text.DecimalFormat;
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
import jimlind.filmlinkd.system.letterboxd.model.LbGenre;
import jimlind.filmlinkd.system.letterboxd.model.LbLanguage;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display information about a film. */
public class FilmEmbedFactory {
  private final EmbedBuilderFactory embedBuilderFactory;
  private final ImageUtils imageUtils;

  /**
   * Constructor for this class.
   *
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model
   * @param imageUtils Assists in finding optimal Letterboxd images
   */
  @Inject
  FilmEmbedFactory(EmbedBuilderFactory embedBuilderFactory, ImageUtils imageUtils) {
    this.embedBuilderFactory = embedBuilderFactory;
    this.imageUtils = imageUtils;
  }

  private static LbFilmStatisticsCounts getStatisticsCounts(CombinedLbFilmModel filmCombination) {
    return filmCombination.getStatisticsCounts();
  }

  private static List<LbGenre> extractGenres(LbFilm film) {
    return film.genres;
  }

  private static LbLanguage extractPrimaryLanguage(LbFilm film) {
    return film.primaryLanguage;
  }

  private static LbFilm extractFilm(CombinedLbFilmModel filmCombination) {
    return filmCombination.getFilm();
  }

  /**
   * Builds the embed.
   *
   * @param filmCombination Film model created from multiple Letterboxd API calls
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public List<MessageEmbed> create(CombinedLbFilmModel filmCombination) {
    EmbedBuilder embedBuilder = embedBuilderFactory.create();
    LbFilm film = extractFilm(filmCombination);

    String releaseYear = film.getReleaseYear() > 0 ? " (" + film.getReleaseYear() + ")" : "";
    embedBuilder.setTitle(film.getName() + releaseYear);
    Object[] urlArgs = {film.getId()};
    embedBuilder.setUrl("https://boxd.it/%s".formatted(urlArgs));
    String imageUrl = imageUtils.getTallest(film.getPoster());
    embedBuilder.setThumbnail(imageUrl.isBlank() ? null : imageUrl);

    StringBuilder descriptionBuilder = new StringBuilder();
    // Add tagline to description
    if (film.tagline != null) {
      descriptionBuilder.append("**").append(film.tagline).append("**\n");
    }

    // Add rating to description
    if (filmCombination.getRating() > 0) {
      String stars = new StarsStringBuilder().setStarCount(filmCombination.getRating()).build();
      DecimalFormat formatter = new DecimalFormat("0.00");
      String rating = formatter.format(filmCombination.getRating());
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
    if (film.getRunTime() > 0) {
      metadata.add(new RuntimeStringBuilder().setRuntime(film.getRunTime()).build());
    }

    String metadataString = "";
    if (!metadata.isEmpty()) {
      metadataString = String.join(", ", metadata) + "\n";
    }

    return metadataString;
  }

  private String createStatistics(CombinedLbFilmModel filmCombination) {
    LbFilmStatisticsCounts counts = getStatisticsCounts(filmCombination);

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
}
