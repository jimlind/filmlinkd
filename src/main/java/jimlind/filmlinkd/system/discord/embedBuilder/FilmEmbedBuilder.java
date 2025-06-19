package jimlind.filmlinkd.system.discord.embedBuilder;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.model.CombinedLBFilmModel;
import jimlind.filmlinkd.system.discord.stringBuilder.*;
import jimlind.filmlinkd.system.letterboxd.model.LBFilm;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmStatisticsCounts;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmSummary;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class FilmEmbedBuilder {
  private final EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;
  private CombinedLBFilmModel filmCombination = null;

  @Inject
  FilmEmbedBuilder(EmbedBuilderFactory embedBuilderFactory, ImageUtils imageUtils) {
    embedBuilder = embedBuilderFactory.create();
    this.imageUtils = imageUtils;
  }

  public FilmEmbedBuilder setFilmCombination(CombinedLBFilmModel filmCombination) {
    this.filmCombination = filmCombination;
    return this;
  }

  public ArrayList<MessageEmbed> build() {
    if (filmCombination == null) {
      return new ArrayList<>();
    }

    LBFilm film = filmCombination.film;
    LBFilmStatistics statistics = filmCombination.filmStatistics;
    LBFilmSummary summary = filmCombination.filmSummary;

    String releaseYear = film.releaseYear > 0 ? String.format(" (%s)", film.releaseYear) : "";
    String imageURL = imageUtils.getTallest(film.poster);

    embedBuilder.setTitle(film.name + releaseYear);
    embedBuilder.setUrl(String.format("https://boxd.it/%s", film.id));
    embedBuilder.setThumbnail(imageURL.isBlank() ? null : imageURL);

    String description = "";
    // Add tagline to description
    if (film.tagline != null) {
      description += String.format("**%s**\n", film.tagline);
    }

    // Add rating to description
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
      metadata.add(new RunTimeStringBuilder().setRunTime(film.runTime).build());
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
    LBFilmStatisticsCounts counts = statistics.counts;
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
