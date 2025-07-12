package jimlind.filmlinkd.system.discord.embedbuilder;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.TextStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.UserStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.*;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class UserEmbedBuilder {
  private final EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;
  private LBMember member = null;
  private LBMemberStatistics memberStatistics = null;

  @Inject
  public UserEmbedBuilder(EmbedBuilderFactory embedBuilderFactory, ImageUtils imageUtils) {
    this.imageUtils = imageUtils;
    embedBuilder = embedBuilderFactory.create();
  }

  public UserEmbedBuilder setMember(LBMember member) {
    this.member = member;
    return this;
  }

  public UserEmbedBuilder setMemberStatistics(LBMemberStatistics memberStatistics) {
    this.memberStatistics = memberStatistics;
    return this;
  }

  public ArrayList<MessageEmbed> build() {
    if (member == null || memberStatistics == null) {
      return new ArrayList<>();
    }

    String displayName = new UserStringBuilder().setUserName(member.displayName).build();
    LBPronoun pronoun = member.pronoun;
    List<String> pronounList =
        List.of(pronoun.subjectPronoun, pronoun.objectPronoun, pronoun.possessivePronoun);

    String description = "";
    if (member.location != null) {
      description += String.format("***%s***\n", member.location);
    }

    if (!member.bio.isBlank()) {
      description += new TextStringBuilder().setHtmlText(member.bio).build(1000);
      description += "\n------------\n";
    }

    Function<LBFilmSummary, String> mapFilmToString =
        film ->
            String.format("- [%s (%s)](https://boxd.it/%s)", film.name, film.releaseYear, film.id);
    List<String> filmStringList = member.favoriteFilms.stream().map(mapFilmToString).toList();
    description += String.join("\n", filmStringList) + "\n";

    description +=
        String.format(
            "Logged films: %s total | %s this year",
            memberStatistics.counts.watches, memberStatistics.counts.filmsInDiaryThisYear);

    embedBuilder.setTitle(displayName + " " + String.join("/", pronounList));
    embedBuilder.setUrl(String.format("https://boxd.it/%s", member.id));
    embedBuilder.setThumbnail(imageUtils.getTallest(member.avatar));
    embedBuilder.setDescription(
        new DescriptionStringBuilder().setDescriptionText(description).build());

    ArrayList<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }
}
