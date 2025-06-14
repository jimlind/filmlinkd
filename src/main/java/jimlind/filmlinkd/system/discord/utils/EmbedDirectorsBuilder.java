package jimlind.filmlinkd.system.discord.utils;

import java.util.List;
import java.util.stream.Collectors;
import jimlind.filmlinkd.system.letterboxd.model.LBContributionType;
import jimlind.filmlinkd.system.letterboxd.model.LBFilmContributions;

public class EmbedDirectorsBuilder {
  public LBFilmContributions contributions;

  public EmbedDirectorsBuilder(List<LBFilmContributions> contributionList) {
    List<LBFilmContributions> filteredContributions =
        contributionList.stream()
            .filter(contributions -> contributions.type.equals(LBContributionType.Director))
            .toList();
    if (!filteredContributions.isEmpty()) {
      this.contributions = filteredContributions.getFirst();
    }
  }

  public String build() {
    if (this.contributions == null) {
      return "";
    }

    String directorLinks =
        contributions.contributors.stream()
            .map(c -> String.format("[%s](https://boxd.it/%s)", c.name, c.id))
            .collect(Collectors.joining(", "));

    return "Director(s): " + directorLinks;
  }
}
