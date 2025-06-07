package jimlind.filmlinkd.system.discord.eventHandler;
/*
import java.util.ArrayList;
import jimlind.filmlinkd.factory.messageEmbed.UserEmbedFactory;
import jimlind.filmlinkd.system.discord.AccountHelper;
import jimlind.filmlinkd.system.letterboxd.api.MemberStatisticsAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.model.LBMemberStatistics;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserHandler implements Handler {
  @Autowired private AccountHelper accountHelper;
  @Autowired private MemberStatisticsAPI memberStatisticsAPI;
  @Autowired private UserEmbedFactory userEmbedFactory;

  public String getEventName() {
    return "user";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LBMember member = this.accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    LBMemberStatistics memberStatistics = this.memberStatisticsAPI.fetch(member.id);
    if (memberStatistics == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList =
        this.userEmbedFactory.create(member, memberStatistics);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
*/
