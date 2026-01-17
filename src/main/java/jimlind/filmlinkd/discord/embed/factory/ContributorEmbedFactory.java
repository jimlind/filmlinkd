package jimlind.filmlinkd.discord.embed.factory;

import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LbContributionStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LbContributor;
import jimlind.filmlinkd.system.letterboxd.model.LbContributorStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LbLink;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display information about a contributor. */
@Singleton
public class ContributorEmbedFactory {
  private final EmbedBuilderFactory embedBuilderFactory;

  /**
   * Constructor for this class.
   *
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model
   */
  @Inject
  public ContributorEmbedFactory(EmbedBuilderFactory embedBuilderFactory) {
    this.embedBuilderFactory = embedBuilderFactory;
  }

  private static List<LbContributionStatistics> getContributions(
      LbContributorStatistics statisticList) {
    return statisticList.getContributions();
  }

  private static LbContributorStatistics getStatistics(LbContributor contributor) {
    return contributor.getStatistics();
  }

  /**
   * Creates the embed.
   *
   * @param contributor Contributor model from Letterboxd API
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public List<MessageEmbed> create(LbContributor contributor) {
    EmbedBuilder embedBuilder = embedBuilderFactory.create();

    Object[] titleArgs = {contributor.getId()};
    embedBuilder.setTitle(contributor.name, "https://boxd.it/%s".formatted(titleArgs));

    List<String> linkStrings = new LinkedList<>();
    for (LbLink link : contributor.getLinks()) {
      String text = String.format("[%s](%s)", link.type, link.url);
      linkStrings.add(text);
    }
    String joinedLinkStrings = String.join(" | ", linkStrings);

    LbContributorStatistics statisticList = getStatistics(contributor);
    List<LbContributionStatistics> contributionList = getContributions(statisticList);
    List<String> contributionStrings = new LinkedList<>();
    for (LbContributionStatistics contribution : contributionList) {
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

    List<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }
}
