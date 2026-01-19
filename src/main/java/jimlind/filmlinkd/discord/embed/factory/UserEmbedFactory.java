package jimlind.filmlinkd.discord.embed.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import javax.inject.Inject;
import jimlind.filmlinkd.core.string.DisplayNameFormatter;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.TextStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberStatisticsCounts;
import jimlind.filmlinkd.system.letterboxd.model.LbPronoun;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display information about a user. */
public class UserEmbedFactory {
  private final EmbedBuilderFactory embedBuilderFactory;
  private final ImageUtils imageUtils;

  /**
   * Constructor for this class.
   *
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model.
   * @param imageUtils Assists in finding optimal Letterboxd images
   */
  @Inject
  public UserEmbedFactory(EmbedBuilderFactory embedBuilderFactory, ImageUtils imageUtils) {
    this.embedBuilderFactory = embedBuilderFactory;
    this.imageUtils = imageUtils;
  }

  private static LbPronoun getPronoun(LbMember member) {
    return member.getPronoun();
  }

  private static LbMemberStatisticsCounts getCounts(LbMemberStatistics memberStatistics) {
    return memberStatistics.getCounts();
  }

  /**
   * Builds the embed.
   *
   * @param member Member model from Letterboxd API
   * @param memberStatistics Member statistics model from Letterboxd API
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public List<MessageEmbed> create(LbMember member, LbMemberStatistics memberStatistics) {
    StringBuilder descriptionBuilder = new StringBuilder(22);
    if (member.location != null) {
      descriptionBuilder.append("***").append(member.getLocation()).append("***\n");
    }

    if (!member.getBio().isBlank()) {
      descriptionBuilder
          .append(new TextStringBuilder().setHtmlText(member.bio).build(1000))
          .append("\n------------\n");
    }

    Function<LbFilmSummary, String> mapFilmToString =
        film ->
            String.format("- [%s (%s)](https://boxd.it/%s)", film.name, film.releaseYear, film.id);
    List<String> filmStringList = member.getFavoriteFilms().stream().map(mapFilmToString).toList();
    descriptionBuilder
        .append(String.join("\n", filmStringList))
        .append('\n')
        .append(
            String.format(
                "Logged films: %s total | %s this year",
                getCounts(memberStatistics).getWatches(),
                getCounts(memberStatistics).getFilmsInDiaryThisYear()));

    String displayName = DisplayNameFormatter.format(member.displayName);
    LbPronoun pronoun = getPronoun(member);
    String pronounString =
        String.join("/", pronoun.subjectPronoun, pronoun.objectPronoun, pronoun.possessivePronoun);

    EmbedBuilder embedBuilder = embedBuilderFactory.create();
    embedBuilder.setTitle(displayName + " (" + pronounString + ")");

    embedBuilder.setUrl("https://boxd.it/" + member.getId());
    embedBuilder.setThumbnail(imageUtils.getTallest(member.avatar));
    embedBuilder.setDescription(
        new DescriptionStringBuilder().setDescriptionText(descriptionBuilder.toString()).build());

    List<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }
}
