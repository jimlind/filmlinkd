package jimlind.filmlinkd.discord.commands.follow;

import java.util.List;
import javax.inject.Inject;
import jimlind.filmlinkd.database.channel.UserChannelDao;
import jimlind.filmlinkd.database.channel.UserChannelRow;
import jimlind.filmlinkd.database.channel.UserChannelRowFactory;
import jimlind.filmlinkd.database.user.UserDao;
import jimlind.filmlinkd.database.user.UserRow;
import jimlind.filmlinkd.database.user.UserRowFactory;
import jimlind.filmlinkd.discord.event.handler.Handler;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles the /follow command to follow a user. */
public class FollowHandler implements Handler {
  private final AccountHelper accountHelper;
  private final FollowEmbedFactory followEmbedFactory;
  private final UserChannelDao userChannelDao;
  private final UserChannelRowFactory userChannelRowFactory;
  private final UserRowFactory userRowFactory;
  private final UserDao userDao;

  /**
   * Constructor for this class.
   *
   * @param accountHelper Handles translating account names to proper data models
   * @param followEmbedFactory Builds the embed for the /follow command
   * @param userChannelDao Handles queries and inserts for user channel data in the local database
   * @param userChannelRowFactory Builds user channel rows for the local database
   * @param userDao Handles queries and inserts for user data in the local database
   * @param userRowFactory Builds user rows for the local database
   */
  @Inject
  public FollowHandler(
      AccountHelper accountHelper,
      FollowEmbedFactory followEmbedFactory,
      UserChannelDao userChannelDao,
      UserChannelRowFactory userChannelRowFactory,
      UserDao userDao,
      UserRowFactory userRowFactory) {
    this.accountHelper = accountHelper;
    this.followEmbedFactory = followEmbedFactory;
    this.userChannelDao = userChannelDao;
    this.userChannelRowFactory = userChannelRowFactory;
    this.userRowFactory = userRowFactory;
    this.userDao = userDao;
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
    String channelId = ChannelHelper.getChannelId(event);
    if (channelId.isBlank()) {
      event.getHook().sendMessage(NO_CHANNEL_FOUND).queue();
      return;
    }

    UserRow userRow = userRowFactory.createFromMember(member);
    boolean userCreateResult = this.userDao.createUser(userRow);

    UserChannelRow userChannelRow = userChannelRowFactory.createFromMember(member, channelId);
    boolean userChannelCreateResult = this.userChannelDao.createUserChannel(userChannelRow);

    if (!userCreateResult || !userChannelCreateResult) {
      event.getHook().sendMessage("Follow Failed").queue();
      return;
    }

    // TODO: Replace this with a direct call to a method instead of creating an event
    //
    //    // Publish command so scraper knows that a new user should be included.
    //    Command command = commandFactory.create(Command.Type.FOLLOW, member.id, "0");
    //    this.pubSubManager.publishCommand(command);

    // Build the message and reply to the deferred command in Discord.
    List<MessageEmbed> messageEmbedList = followEmbedFactory.create(member);
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
