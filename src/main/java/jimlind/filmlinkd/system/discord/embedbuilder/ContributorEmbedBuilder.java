package jimlind.filmlinkd.system.discord.embedbuilder;

import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LbContributionStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LbContributor;
import jimlind.filmlinkd.system.letterboxd.model.LbLink;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display information about a contributor. */
public class ContributorEmbedBuilder {
  private final EmbedBuilder embedBuilder;
  private LbContributor contributor;

  /**
   * Constructor for this class.
   *
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model
   */
  @Inject
  public ContributorEmbedBuilder(EmbedBuilderFactory embedBuilderFactory) {
    embedBuilder = embedBuilderFactory.create();
  }

  /**
   * Setter for the contributor attribute.
   *
   * @param contributor Contributor model from Letterboxd API
   * @return This class for chaining
   */
  public ContributorEmbedBuilder setContributor(LbContributor contributor) {
    this.contributor = contributor;
    return this;
  }

  /**
   * Builds the embed.
   *
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public List<MessageEmbed> build() {
    if (contributor == null) {
      return new ArrayList<>();
    }

    embedBuilder.setTitle(contributor.name, String.format("https://boxd.it/%s", contributor.id));

    List<String> linkStrings = new LinkedList<>();
    for (LbLink link : contributor.getLinks()) {
      String text = String.format("[%s](%s)", link.type, link.url);
      linkStrings.add(text);
    }
    String joinedLinkStrings = String.join(" | ", linkStrings);

    List<String> contributionStrings = new LinkedList<>();
    for (LbContributionStatistics contribution : contributor.getStatistics().getContributions()) {
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
