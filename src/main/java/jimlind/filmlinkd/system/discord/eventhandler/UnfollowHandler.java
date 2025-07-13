package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import jimlind.filmlinkd.system.discord.embedbuilder.UnfollowEmbedBuilder;
import jimlind.filmlinkd.system.discord.helper.AccountHelper;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;
import jimlind.filmlinkd.system.google.FirestoreManager;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class UnfollowHandler implements Handler {
  private final AccountHelper accountHelper;
  private final ChannelHelper channelHelper;
  private final FirestoreManager firestoreManager;
  private final UnfollowEmbedBuilder unfollowEmbedBuilder;

  @Inject
  UnfollowHandler(
      AccountHelper accountHelper,
      ChannelHelper channelHelper,
      FirestoreManager firestoreManager,
      UnfollowEmbedBuilder unfollowEmbedBuilder) {
    this.accountHelper = accountHelper;
    this.channelHelper = channelHelper;
    this.firestoreManager = firestoreManager;
    this.unfollowEmbedBuilder = unfollowEmbedBuilder;
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

    if (!firestoreManager.removeUserSubscription(member.id, channelId)) {
      event.getHook().sendMessage("Unfollow Failed").queue();
      return;
    }

    ArrayList<MessageEmbed> messageEmbedList = unfollowEmbedBuilder.setMember(member).build();
    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }
}
