package jimlind.filmlinkd.discord.event.handler;

import com.google.inject.Inject;
import java.util.List;
import jimlind.filmlinkd.discord.embed.factory.FollowEmbedFactory;
import jimlind.filmlinkd.factory.CommandFactory;
import jimlind.filmlinkd.google.pubsub.PubSubManagerInterface;
import jimlind.filmlinkd.model.Command;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.UserCoordinator;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles the /follow command to follow a user and show new diary entries in specified channel. */
public class FollowHandler implements Handler {
  private final AccountHelper accountHelper;
  private final ChannelHelper channelHelper;
  private final CommandFactory commandFactory;
  private final FollowEmbedFactory followEmbedFactory;
  private final PubSubManagerInterface pubSubManager;
  private final UserCoordinator userCoordinator;

  /**
   * Constructor for this class.
   *
   * @param accountHelper Handles translating account names to proper data models
   * @param channelHelper Service that parses a channel id from a slash event with options
   * @param commandFactory Builds the command object that is pushed into the PubSub system
   * @param followEmbedFactory Builds the embed for the /follow command
   * @param pubSubManager Handles the PubSub system to accept commands and messages
   * @param userCoordinator Handles the interactions with the user model
   */
  @Inject
  public FollowHandler(
      AccountHelper accountHelper,
      ChannelHelper channelHelper,
      CommandFactory commandFactory,
      FollowEmbedFactory followEmbedFactory,
      PubSubManagerInterface pubSubManager,
      UserCoordinator userCoordinator) {
    this.accountHelper = accountHelper;
    this.channelHelper = channelHelper;
    this.commandFactory = commandFactory;
    this.followEmbedFactory = followEmbedFactory;
    this.pubSubManager = pubSubManager;
    this.userCoordinator = userCoordinator;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    // Exit early if no account was found.
    LbMember member = accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    // Exit early if no channel was found.
    String channelId = channelHelper.getChannelId(event);
    if (channelId.isBlank()) {
      event.getHook().sendMessage(NO_CHANNEL_FOUND).queue();
      return;
    }

    // Exit early if database transaction failed.
    User user = userCoordinator.follow(member, channelId);
    if (user == null) {
      event.getHook().sendMessage("Follow Failed").queue();
      return;
    }

    // Publish command so scraper knows that a new user should be included.
    Command command = commandFactory.create(Command.Type.FOLLOW, member.id, "0");
    this.pubSubManager.publishCommand(command);

    // Build the message and reply to the deferred command in Discord.
    List<MessageEmbed> messageEmbedList = followEmbedFactory.create(member);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
