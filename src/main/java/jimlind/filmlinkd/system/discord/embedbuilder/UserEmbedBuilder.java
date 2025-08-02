package jimlind.filmlinkd.system.discord.embedbuilder;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.TextStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.UserStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberStatistics;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberStatisticsCounts;
import jimlind.filmlinkd.system.letterboxd.model.LbPronoun;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display information about a user. */
public class UserEmbedBuilder {
  private final EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;
  private LbMember member;
  private LbMemberStatistics memberStatistics;

  /**
   * Constructor for this class.
   *
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model.
   * @param imageUtils Assists in finding optimal Letterboxd images
   */
  @Inject
  public UserEmbedBuilder(EmbedBuilderFactory embedBuilderFactory, ImageUtils imageUtils) {
    this.imageUtils = imageUtils;
    embedBuilder = embedBuilderFactory.create();
  }

  /**
   * Setter for the member attribute.
   *
   * @param member Member model from Letterboxd API
   * @return This class for chaining
   */
  public UserEmbedBuilder setMember(LbMember member) {
    this.member = member;
    return this;
  }

  /**
   * Setter for the memberStatistics attribute.
   *
   * @param memberStatistics Member statistics model from Letterboxd API
   * @return This class for chaining
   */
  public UserEmbedBuilder setMemberStatistics(LbMemberStatistics memberStatistics) {
    this.memberStatistics = memberStatistics;
    return this;
  }

  /**
   * Builds the embed.
   *
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public List<MessageEmbed> build() {
    if (member == null || memberStatistics == null) {
      return new ArrayList<>();
    }

    StringBuilder descriptionBuilder = new StringBuilder();
    if (member.location != null) {
      descriptionBuilder.append(String.format("***%s***\n", member.location));
    }

    if (!member.bio.isBlank()) {
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
                getCounts().getWatches(), getCounts().getFilmsInDiaryThisYear()));

    String displayName = new UserStringBuilder().setUsername(member.displayName).build();
    LbPronoun pronoun = getPronoun();
    String pronounString =
        String.join("/", pronoun.subjectPronoun, pronoun.objectPronoun, pronoun.possessivePronoun);
    embedBuilder.setTitle(displayName + " " + pronounString);

    embedBuilder.setUrl(String.format("https://boxd.it/%s", member.id));
    embedBuilder.setThumbnail(imageUtils.getTallest(member.avatar));
    embedBuilder.setDescription(
        new DescriptionStringBuilder().setDescriptionText(descriptionBuilder.toString()).build());

    List<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }

  private LbPronoun getPronoun() {
    return member.getPronoun();
  }

  private LbMemberStatisticsCounts getCounts() {
    return memberStatistics.getCounts();
  }
}
