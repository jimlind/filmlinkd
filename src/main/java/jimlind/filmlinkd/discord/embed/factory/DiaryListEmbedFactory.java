package jimlind.filmlinkd.discord.embed.factory;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.core.string.DisplayNameFormatter;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.StarsStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LbDiaryDetails;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display information about a list of diary entries. */
@Singleton
public class DiaryListEmbedFactory {
  private final EmbedBuilderFactory embedBuilderFactory;
  private final ImageUtils imageUtils;

  /**
   * Constructor for this class.
   *
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model
   * @param imageUtils Assists in finding optimal Letterboxd images
   */
  @Inject
  public DiaryListEmbedFactory(EmbedBuilderFactory embedBuilderFactory, ImageUtils imageUtils) {
    this.embedBuilderFactory = embedBuilderFactory;
    this.imageUtils = imageUtils;
  }

  private static LbFilmSummary getFilm(LbLogEntry logEntry) {
    return logEntry.getFilm();
  }

  private static LbDiaryDetails extractDiaryDetails(LbLogEntry logEntry) {
    return logEntry.getDiaryDetails();
  }

  /**
   * Builds the embed.
   *
   * @param member Member model from Letterboxd API
   * @param logEntryList Log Entry list from Letterboxd API
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
  public List<MessageEmbed> create(LbMember member, List<LbLogEntry> logEntryList) {
    EmbedBuilder embedBuilder = embedBuilderFactory.create();

    List<String> entryList = new ArrayList<>();
    for (LbLogEntry logEntry : logEntryList) {
      LbFilmSummary film = getFilm(logEntry);
      String firstLine =
          String.format(
              "[**%s (%s)**](https://boxd.it/%s)", film.name, film.releaseYear, logEntry.id);
      entryList.add(firstLine);

      StringBuilder secondLineBuilder = new StringBuilder(66);
      LbDiaryDetails diaryDetails = extractDiaryDetails(logEntry);
      if (diaryDetails != null) {
        secondLineBuilder.append(diaryDetails.diaryDate);
      }

      secondLineBuilder
          .append(' ')
          .append(new StarsStringBuilder().setStarCount(logEntry.getRating()).build());
      if (diaryDetails != null && diaryDetails.isRewatch()) {
        secondLineBuilder.append(" <:r:851135667546488903>");
      }

      secondLineBuilder
          .append(logEntry.isLike() ? " <:l:851138401557676073>" : "")
          .append(logEntry.getReview() != null ? " :speech_balloon:" : "");
      entryList.add(secondLineBuilder.toString());
    }

    String out = String.join("\n", entryList);
    embedBuilder.setDescription(out);

    String displayName = DisplayNameFormatter.format(member.displayName);
    embedBuilder.setTitle(String.format("Recent Diary Activity from %s", displayName));
    Object[] urlArgs = {member.getId()};
    embedBuilder.setUrl("https://boxd.it/%s".formatted(urlArgs));
    embedBuilder.setThumbnail(imageUtils.getTallest(member.avatar));

    List<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }
}
