package jimlind.filmlinkd.system.discord.stringbuilder;

import java.util.List;
import java.util.stream.Collectors;
import jimlind.filmlinkd.system.letterboxd.model.LBContributionType;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmContributions;

public class DirectorsStringBuilder {
  public LBFilmContributions directors = null;

  public DirectorsStringBuilder setContributionList(List<LBFilmContributions> contributionList) {
    List<LBFilmContributions> filteredContributions =
        contributionList.stream()
            .filter(contributions -> contributions.type.equals(LBContributionType.Director))
            .toList();
    if (!filteredContributions.isEmpty()) {
      directors = filteredContributions.getFirst();
    }

    return this;
  }

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
