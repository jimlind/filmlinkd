package jimlind.filmlinkd.system.discord.embedBuilder;

import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.system.discord.utils.EmbedBuilder;
import jimlind.filmlinkd.system.discord.utils.EmbedStarsBuilder;
import jimlind.filmlinkd.system.discord.utils.EmbedUserBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class DiaryListEmbedBuilder {
  public ArrayList<MessageEmbed> build(LBMember member, List<LBLogEntry> logEntryList) {
    EmbedBuilder embedBuilder = new EmbedBuilder();

    ArrayList<String> entryList = new ArrayList<>();
    for (LBLogEntry logEntry : logEntryList) {
      String firstLine =
          String.format(
              "[**%s (%s)**](https://boxd.it/%s)",
              logEntry.film.name, logEntry.film.releaseYear, logEntry.id);

      String secondLine = logEntry.diaryDetails != null ? logEntry.diaryDetails.diaryDate : "";
      secondLine += " " + new EmbedStarsBuilder(logEntry.rating).build();
      secondLine +=
          logEntry.diaryDetails != null && logEntry.diaryDetails.rewatch
              ? " <:r:851135667546488903>"
              : "";
      secondLine += logEntry.like ? " <:l:851138401557676073>" : "";
      secondLine += logEntry.review != null ? " :speech_balloon:" : "";

      entryList.add(firstLine + "\n" + secondLine);
    }

    String out = String.join("\n", entryList);
    embedBuilder.setDescription(out);

    embedBuilder.setTitle(
        String.format(
            "Recent Diary Activity from %s", new EmbedUserBuilder(member.displayName).build()));
    embedBuilder.setUrl(String.format("https://boxd.it/%s", member.id));
    embedBuilder.setThumbnail(ImageUtils.getTallest(member.avatar));

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
