package jimlind.filmlinkd.system.discord.eventhandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.system.discord.embedbuilder.HelpEmbedBuilder;
import jimlind.filmlinkd.system.google.FirestoreManager;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/** Handles the /help command to show a help message and allow users to test the bot. */
public class HelpHandler implements Handler {
  private final FirestoreManager firestoreManager;
  private final HelpEmbedBuilder helpEmbedBuilder;

  /**
   * Constructor for this class.
   *
   * @param firestoreManager Service that handles all Firestore interactions
   * @param helpEmbedBuilder Builds the embed for the /help command
   */
  @Inject
  HelpHandler(FirestoreManager firestoreManager, HelpEmbedBuilder helpEmbedBuilder) {
    this.firestoreManager = firestoreManager;
    this.helpEmbedBuilder = helpEmbedBuilder;
  }

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("test");
    boolean testStatus = optionMapping != null && optionMapping.getAsBoolean();
    if (testStatus) {
      event.getHook().sendMessageEmbeds(helpEmbedBuilder.createTestMessage()).queue();
      this.queueAdditionalTestMessages(event.getMessageChannel());
      return;
    }

    long userCount = firestoreManager.getUserCount();
    long guildCount =
        event.getJDA().getShardManager() != null
            ? event.getJDA().getShardManager().getGuildCache().size()
            : event.getJDA().getGuildCache().size();
    ArrayList<MessageEmbed> messageEmbedList = helpEmbedBuilder.create(userCount, guildCount);

    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }

  private void queueAdditionalTestMessages(MessageChannel channel) {
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    final int[] count = {1};
    Runnable task =
        () -> {
          try {
            channel.sendMessageEmbeds(helpEmbedBuilder.createTestMessage(count[0])).queue();
          } catch (Exception e) {
            // Ignore any issues
          }
          if (count[0] >= 4) {
            scheduler.shutdown();
          }
          count[0]++;
        };

    scheduler.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
  }
}
