package jimlind.filmlinkd.system.discord.eventHandler;
/*
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.factory.messageEmbed.DiaryListEmbedFactory;
import jimlind.filmlinkd.system.discord.AccountHelper;
import jimlind.filmlinkd.system.letterboxd.api.LogEntriesAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DiaryHandler implements Handler {
  @Autowired private AccountHelper accountHelper;
  @Autowired private DiaryListEmbedFactory diaryListEmbedFactory;
  @Autowired private LogEntriesAPI logEntriesAPI;

  public String getEventName() {
    return "diary";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LBMember member = this.accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    List<LBLogEntry> logEntryList = this.logEntriesAPI.getRecentForUser(member.id, 5);
    if (logEntryList.isEmpty()) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList =
        this.diaryListEmbedFactory.create(member, logEntryList);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
*/
