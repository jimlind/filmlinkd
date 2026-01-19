package jimlind.filmlinkd.discord.event.handler;

import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.discord.embed.factory.UnfollowEmbedFactory;
import jimlind.filmlinkd.google.db.UserWriter;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles the /unfollow command to unfollow a user and stop showing new diary entries. */
@Singleton
public class UnfollowHandler implements Handler {
  private final AccountHelper accountHelper;
  private final UnfollowEmbedFactory unfollowEmbedFactory;
  private final UserWriter userWriter;

  /**
   * Constructor for this class.
   *
   * @param accountHelper Handles translating account names to proper data models
   * @param unfollowEmbedFactory Builds the embed for the /unfollow command
   * @param userWriter Handles all write operations for user data in Firestore
   */
  @Inject
  UnfollowHandler(
      AccountHelper accountHelper,
      UnfollowEmbedFactory unfollowEmbedFactory,
      UserWriter userWriter) {
    this.accountHelper = accountHelper;
    this.unfollowEmbedFactory = unfollowEmbedFactory;
    this.userWriter = userWriter;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LbMember member = accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    String channelId = ChannelHelper.getChannelId(event);
    if (channelId.isBlank()) {
      event.getHook().sendMessage(NO_CHANNEL_FOUND).queue();
      return;
    }

    if (!userWriter.removeUserSubscription(member.id, channelId)) {
      event.getHook().sendMessage("Unfollow Failed").queue();
      return;
    }

    List<MessageEmbed> messageEmbedList = unfollowEmbedFactory.create(member);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
