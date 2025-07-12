package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.system.discord.embedbuilder.DiaryListEmbedBuilder;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesApi;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class DiaryHandler implements Handler {
  private final AccountHelper accountHelper;
  private final DiaryListEmbedBuilder diaryListEmbedBuilder;
  private final LogEntriesApi logEntriesApi;

  @Inject
  DiaryHandler(
      AccountHelper accountHelper,
      DiaryListEmbedBuilder diaryListEmbedBuilder,
      LogEntriesApi logEntriesApi) {
    this.accountHelper = accountHelper;
    this.diaryListEmbedBuilder = diaryListEmbedBuilder;
    this.logEntriesApi = logEntriesApi;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LBMember member = accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    List<LBLogEntry> logEntryList = logEntriesApi.getRecentForUser(member.id, 5);
    if (logEntryList.isEmpty()) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList =
        diaryListEmbedBuilder.setMember(member).setLogEntryList(logEntryList).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
