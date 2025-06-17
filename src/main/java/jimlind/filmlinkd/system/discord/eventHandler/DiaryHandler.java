package jimlind.filmlinkd.system.discord.eventHandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.system.discord.AccountHelper;
import jimlind.filmlinkd.system.discord.embedBuilder.DiaryListEmbedBuilder;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class DiaryHandler implements Handler {
  private final AccountHelper accountHelper;
  private final DiaryListEmbedBuilder diaryListEmbedBuilder;
  private final LogEntriesAPI logEntriesAPI;

  @Inject
  DiaryHandler(
      AccountHelper accountHelper,
      DiaryListEmbedBuilder diaryListEmbedBuilder,
      LogEntriesAPI logEntriesAPI) {
    this.accountHelper = accountHelper;
    this.diaryListEmbedBuilder = diaryListEmbedBuilder;
    this.logEntriesAPI = logEntriesAPI;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LBMember member = accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    List<LBLogEntry> logEntryList = logEntriesAPI.getRecentForUser(member.id, 5);
    if (logEntryList.isEmpty()) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList =
        diaryListEmbedBuilder.buildEmbedList(member, logEntryList);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
