package jimlind.filmlinkd.system.discord.eventHandler;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.system.discord.embedBuilder.HelpEmbedBuilder;
import jimlind.filmlinkd.system.google.FirestoreManager;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class HelpHandler implements Handler {
  private final FirestoreManager firestoreManager;
  private final HelpEmbedBuilder helpEmbedBuilder;

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

    String name = getClass().getPackage().getImplementationTitle();
    String version = getClass().getPackage().getImplementationVersion();
    long userCount = firestoreManager.getUserCount();
    long guildCount =
        event.getJDA().getShardManager() != null
            ? event.getJDA().getShardManager().getGuildCache().size()
            : event.getJDA().getGuildCache().size();
    ArrayList<MessageEmbed> messageEmbedList =
        helpEmbedBuilder.create(name, version, userCount, guildCount);

    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }

  private void queueAdditionalTestMessages(MessageChannel channel) {
    try (ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1)) {
      TimerTask task =
          new TimerTask() {
            private int count = 1;

            public void run() {
              try {
                channel.sendMessageEmbeds(helpEmbedBuilder.createTestMessage(count)).queue();
              } catch (Exception e) {
                // Ignore any issues. Users will determine if things succeed.
              }
              if (count >= 4) {
                scheduler.shutdown();
              }
              count++;
            }
          };
      scheduler.scheduleAtFixedRate(task, 1, 1, TimeUnit.SECONDS);
    }
  }
}
