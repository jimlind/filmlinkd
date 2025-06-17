package jimlind.filmlinkd.system.discord.embedBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import jimlind.filmlinkd.system.discord.stringBuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringBuilder.TextStringBuilder;
import jimlind.filmlinkd.system.discord.stringBuilder.UserStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.*;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class UserEmbedBuilder {
  public ArrayList<MessageEmbed> buildEmbedList(
      LBMember member, LBMemberStatistics memberStatistics) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

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
    embedBuilder.setThumbnail(ImageUtils.getTallest(member.avatar));
    embedBuilder.setDescription(
        new DescriptionStringBuilder().setDescriptionText(description).build());

    ArrayList<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }
}
