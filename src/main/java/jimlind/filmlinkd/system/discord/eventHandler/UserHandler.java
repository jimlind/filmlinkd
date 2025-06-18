package jimlind.filmlinkd.system.discord.eventHandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import jimlind.filmlinkd.system.discord.AccountHelper;
import jimlind.filmlinkd.system.discord.embedBuilder.UserEmbedBuilder;
import jimlind.filmlinkd.system.letterboxd.api.MemberStatisticsAPI;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.model.LBMemberStatistics;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class UserHandler implements Handler {
  private final AccountHelper accountHelper;
  private final MemberStatisticsAPI memberStatisticsAPI;
  private final UserEmbedBuilder userEmbedBuilder;

  @Inject
  UserHandler(
      AccountHelper accountHelper,
      MemberStatisticsAPI memberStatisticsAPI,
      UserEmbedBuilder userEmbedBuilder) {
    this.accountHelper = accountHelper;
    this.memberStatisticsAPI = memberStatisticsAPI;
    this.userEmbedBuilder = userEmbedBuilder;
  }

  @Override
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
        this.userEmbedBuilder.setMember(member).setMemberStatistics(memberStatistics).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
