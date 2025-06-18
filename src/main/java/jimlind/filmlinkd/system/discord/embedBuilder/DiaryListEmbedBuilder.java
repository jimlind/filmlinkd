package jimlind.filmlinkd.system.discord.embedBuilder;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringBuilder.StarsStringBuilder;
import jimlind.filmlinkd.system.discord.stringBuilder.UserStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class DiaryListEmbedBuilder {
  private final EmbedBuilder embedBuilder;
  private LBMember member = null;
  private List<LBLogEntry> logEntryList = new ArrayList<>();

  @Inject
  public DiaryListEmbedBuilder(EmbedBuilderFactory embedBuilderFactory) {
    embedBuilder = embedBuilderFactory.create();
  }

  public DiaryListEmbedBuilder setMember(LBMember member) {
    this.member = member;
    return this;
  }

  public DiaryListEmbedBuilder setLogEntryList(List<LBLogEntry> logEntryList) {
    this.logEntryList = logEntryList;
    return this;
  }

  public ArrayList<MessageEmbed> build() {
    if (member == null) {
      return new ArrayList<>();
    }

    ArrayList<String> entryList = new ArrayList<>();
    for (LBLogEntry logEntry : logEntryList) {
      String firstLine =
          String.format(
              "[**%s (%s)**](https://boxd.it/%s)",
              logEntry.film.name, logEntry.film.releaseYear, logEntry.id);

      String secondLine = logEntry.diaryDetails != null ? logEntry.diaryDetails.diaryDate : "";
      secondLine += " " + new StarsStringBuilder().setStarCount(logEntry.rating).build();
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
            "Recent Diary Activity from %s",
            new UserStringBuilder().setUserName(member.displayName).build()));
    embedBuilder.setUrl(String.format("https://boxd.it/%s", member.id));
    embedBuilder.setThumbnail(ImageUtils.getTallest(member.avatar));

    ArrayList<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }
}
