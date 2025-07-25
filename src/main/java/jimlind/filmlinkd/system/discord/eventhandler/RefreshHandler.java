package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import jimlind.filmlinkd.system.discord.embedbuilder.RefreshEmbedBuilder;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

/** Handles the /refresh command that updates cached user data like name and image. */
public class RefreshHandler implements Handler {
  private final AccountHelper accountHelper;
  private final FirestoreManager firestoreManager;
  private final RefreshEmbedBuilder refreshEmbedBuilder;

  /**
   * Constructor for this class.
   *
   * @param accountHelper Handles translating account names to proper data models
   * @param firestoreManager Service that handles all Firestore interactions
   * @param refreshEmbedBuilder Builds the embed for the /refresh command
   */
  @Inject
  RefreshHandler(
      AccountHelper accountHelper,
      FirestoreManager firestoreManager,
      RefreshEmbedBuilder refreshEmbedBuilder) {
    this.accountHelper = accountHelper;
    this.firestoreManager = firestoreManager;
    this.refreshEmbedBuilder = refreshEmbedBuilder;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    LbMember member = accountHelper.getMember(event);
    if (member == null) {
      event.getHook().sendMessage(NO_RESULTS_FOUND).queue();
      return;
    }

    if (!this.firestoreManager.updateUserDisplayData(member)) {
      event.getHook().sendMessage("Refresh Failed").queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = refreshEmbedBuilder.setMember(member).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
