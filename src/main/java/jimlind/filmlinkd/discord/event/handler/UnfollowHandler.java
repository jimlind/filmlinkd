package jimlind.filmlinkd.discord.event.handler;

import com.google.inject.Inject;
import java.util.List;
import jimlind.filmlinkd.discord.embed.factory.UnfollowEmbedFactory;
import jimlind.filmlinkd.google.db.UserWriterInterface;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles the /unfollow command to unfollow a user and stop showing new diary entries. */
public class UnfollowHandler implements Handler {
  private final AccountHelper accountHelper;
  private final ChannelHelper channelHelper;
  private final UnfollowEmbedFactory unfollowEmbedFactory;
  private final UserWriterInterface userWriter;

  /**
   * Constructor for this class.
   *
   * @param accountHelper Handles translating account names to proper data models
   * @param channelHelper Service that parses a channel id from a slash event with options
   * @param unfollowEmbedFactory Builds the embed for the /unfollow command
   * @param userWriter Handles all write operations for user data in Firestore
   */
  @Inject
  UnfollowHandler(
      AccountHelper accountHelper,
      ChannelHelper channelHelper,
      UnfollowEmbedFactory unfollowEmbedFactory,
      UserWriterInterface userWriter) {
    this.accountHelper = accountHelper;
    this.channelHelper = channelHelper;
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

    String channelId = channelHelper.getChannelId(event);
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
