package jimlind.filmlinkd.system.discord.stringbuilder;

import java.util.List;
import java.util.stream.Collectors;
import jimlind.filmlinkd.system.letterboxd.model.LbContributionType;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmContributions;

/** Build a string that displays a list of directors with Letterboxd links. */
public class DirectorsStringBuilder {
  /** The Directors. */
  public LbFilmContributions directors = null;

  /**
   * Setter for the contributionList attribute.
   *
   * @param contributionList The list of all contributions
   * @return This class for chaining
   */
  public DirectorsStringBuilder setContributionList(List<LbFilmContributions> contributionList) {
    List<LbFilmContributions> filteredContributions =
        contributionList.stream()
            .filter(contributions -> contributions.type.equals(LbContributionType.Director))
            .toList();
    if (!filteredContributions.isEmpty()) {
      directors = filteredContributions.getFirst();
    }

    return this;
  }

  /**
   * Builds the string.
   *
   * @return The list of directors with Letterboxd links
   */
  public String build() {
    if (directors == null) {
      return "";
    }

    String directorLinks =
        directors.contributors.stream()
            .map(c -> String.format("[%s](https://boxd.it/%s)", c.name, c.id))
            .collect(Collectors.joining(", "));

    return "Director(s): " + directorLinks;
  }
}
