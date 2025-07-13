package jimlind.filmlinkd.system.discord.embedbuilder;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.StarsStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.UserStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class DiaryListEmbedBuilder {
  private final EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;

  private LbMember member = null;
  private List<LbLogEntry> logEntryList = new ArrayList<>();

  @Inject
  public DiaryListEmbedBuilder(EmbedBuilderFactory embedBuilderFactory, ImageUtils imageUtils) {
    embedBuilder = embedBuilderFactory.create();
    this.imageUtils = imageUtils;
  }

  public DiaryListEmbedBuilder setMember(LbMember member) {
    this.member = member;
    return this;
  }

  public DiaryListEmbedBuilder setLogEntryList(List<LbLogEntry> logEntryList) {
    this.logEntryList = logEntryList;
    return this;
  }

  public ArrayList<MessageEmbed> build() {
    if (member == null) {
      return new ArrayList<>();
    }

    ArrayList<String> entryList = new ArrayList<>();
    for (LbLogEntry logEntry : logEntryList) {
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
    embedBuilder.setThumbnail(imageUtils.getTallest(member.avatar));

    ArrayList<MessageEmbed> embedList = new ArrayList<>();
    embedList.add(embedBuilder.build());

    return embedList;
  }
}
