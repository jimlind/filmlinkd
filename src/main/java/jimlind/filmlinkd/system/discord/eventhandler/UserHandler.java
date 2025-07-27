package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.util.List;
import jimlind.filmlinkd.system.discord.embedbuilder.UserEmbedBuilder;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.letterboxd.api.MemberStatisticsApi;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberStatistics;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles the /user command to show a user matching the search input. */
public class UserHandler implements Handler {
  private final AccountHelper accountHelper;
  private final MemberStatisticsApi memberStatisticsApi;
  private final UserEmbedBuilder userEmbedBuilder;

  /**
   * Constructor for this class.
   *
   * @param accountHelper Handles translating account names to proper data models
   * @param memberStatisticsApi Fetches member statistics data from Letterboxd API
   * @param userEmbedBuilder Builds the embed for the /user command
   */
  @Inject
  UserHandler(
      AccountHelper accountHelper,
      MemberStatisticsApi memberStatisticsApi,
      UserEmbedBuilder userEmbedBuilder) {
    this.accountHelper = accountHelper;
    this.memberStatisticsApi = memberStatisticsApi;
    this.userEmbedBuilder = userEmbedBuilder;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LbMember member = this.accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    LbMemberStatistics memberStatistics = this.memberStatisticsApi.fetch(member.id);
    if (memberStatistics == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    List<MessageEmbed> messageEmbedList =
        this.userEmbedBuilder.setMember(member).setMemberStatistics(memberStatistics).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
