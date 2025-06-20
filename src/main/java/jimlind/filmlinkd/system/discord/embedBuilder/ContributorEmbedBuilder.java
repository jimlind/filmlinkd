package jimlind.filmlinkd.system.discord.embedBuilder;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringBuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LBContributionStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LBContributor;
import jimlind.filmlinkd.system.letterboxd.model.LBLink;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class ContributorEmbedBuilder {
  private final EmbedBuilder embedBuilder;
  private LBContributor contributor = null;

  @Inject
  public ContributorEmbedBuilder(EmbedBuilderFactory embedBuilderFactory) {
    embedBuilder = embedBuilderFactory.create();
  }

  public ContributorEmbedBuilder setContributor(LBContributor contributor) {
    this.contributor = contributor;
    return this;
  }

  public ArrayList<MessageEmbed> build() {
    if (contributor == null) {
      return new ArrayList<>();
    }

    embedBuilder.setTitle(contributor.name, String.format("https://boxd.it/%s", contributor.id));

    List<String> linkStrings = new LinkedList<>();
    for (LBLink link : contributor.links) {
      String text = String.format("[%s](%s)", link.type, link.url);
      linkStrings.add(text);
    }
    String joinedLinkStrings = String.join(" | ", linkStrings);

    List<String> contributionStrings = new LinkedList<>();
    for (LBContributionStatistics contribution : contributor.statistics.contributions) {
      String text = String.format("**%s:** %s", contribution.type, contribution.filmCount);
      contributionStrings.add(text);
    }
    String joinedContributionStrings = String.join("\n", contributionStrings);

    List<String> descriptionElements =
        Lists.newArrayList(joinedLinkStrings, joinedContributionStrings, contributor.bio);

    descriptionElements.removeAll(Collections.singleton(null));
    embedBuilder.setDescription(
        new DescriptionStringBuilder()
            .setDescriptionText(String.join("\n\n", descriptionElements))
            .build());

    ArrayList<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }
}
