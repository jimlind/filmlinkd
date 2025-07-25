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
import jimlind.filmlinkd.system.letterboxd.model.LbPronoun;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display information about a user. */
public class UserEmbedBuilder {
  private final EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;
  private LbMember member = null;
  private LbMemberStatistics memberStatistics = null;

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

    String description = "";
    if (member.location != null) {
      description += String.format("***%s***\n", member.location);
    }

    if (!member.bio.isBlank()) {
      description += new TextStringBuilder().setHtmlText(member.bio).build(1000);
      description += "\n------------\n";
    }

    Function<LbFilmSummary, String> mapFilmToString =
        film ->
            String.format("- [%s (%s)](https://boxd.it/%s)", film.name, film.releaseYear, film.id);
    List<String> filmStringList = member.favoriteFilms.stream().map(mapFilmToString).toList();
    description += String.join("\n", filmStringList) + "\n";

    description +=
        String.format(
            "Logged films: %s total | %s this year",
            memberStatistics.counts.watches, memberStatistics.counts.filmsInDiaryThisYear);

    String displayName = new UserStringBuilder().setUsername(member.displayName).build();
    LbPronoun pronoun = member.pronoun;
    List<String> pronounList =
        List.of(pronoun.subjectPronoun, pronoun.objectPronoun, pronoun.possessivePronoun);
    embedBuilder.setTitle(displayName + " " + String.join("/", pronounList));

    embedBuilder.setUrl(String.format("https://boxd.it/%s", member.id));
    embedBuilder.setThumbnail(imageUtils.getTallest(member.avatar));
    embedBuilder.setDescription(
        new DescriptionStringBuilder().setDescriptionText(description).build());

    List<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }
}
