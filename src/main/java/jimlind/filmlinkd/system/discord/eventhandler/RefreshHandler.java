package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.util.List;
import jimlind.filmlinkd.system.discord.embedbuilder.RefreshEmbedFactory;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.google.firestore.UserWriter;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles the /refresh command that updates cached user data like name and image. */
public class RefreshHandler implements Handler {
  private final AccountHelper accountHelper;
  private final RefreshEmbedFactory refreshEmbedFactory;
  private final UserWriter userWriter;

  /**
   * Constructor for this class.
   *
   * @param accountHelper Handles translating account names to proper data models
   * @param refreshEmbedFactory Builds the embed for the /refresh command
   * @param userWriter Handles all write operations for user data in Firestore
   */
  @Inject
  RefreshHandler(
      AccountHelper accountHelper, RefreshEmbedFactory refreshEmbedFactory, UserWriter userWriter) {
    this.accountHelper = accountHelper;
    this.refreshEmbedFactory = refreshEmbedFactory;
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

    if (!userWriter.updateUserDisplayData(member)) {
      event.getHook().sendMessage("Refresh Failed").queue();
      return;
    }

    List<MessageEmbed> messageEmbedList = refreshEmbedFactory.setMember(member).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
