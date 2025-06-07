package jimlind.filmlinkd.system.discord.eventHandler;

import com.google.inject.Inject;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class HelpHandler implements Handler {

  @Inject
  HelpHandler() {}

  @Override
  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();
    event.getHook().sendMessage("Help command is currently disabled.").queue();
  }
}
/*
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import jimlind.filmlinkd.factory.messageEmbed.HelpEmbedFactory;
import jimlind.filmlinkd.system.google.FirestoreManager;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class HelpHandler implements Handler {
  @Autowired private FirestoreManager firestoreManager;
  @Autowired private HelpEmbedFactory helpEmbedFactory;

  public String getEventName() {
    return "help";
  }

  public void handleEvent(SlashCommandInteractionEvent event) {
    event.deferReply().queue();

    OptionMapping optionMapping = event.getInteraction().getOption("test");
    boolean testStatus = optionMapping != null && optionMapping.getAsBoolean();
    if (testStatus) {
      event.getHook().sendMessageEmbeds(this.helpEmbedFactory.createTestMessage()).queue();
      this.queueAdditionalTestMessages(event.getMessageChannel());
      return;
    }

    String name = getClass().getPackage().getImplementationTitle();
    String version = getClass().getPackage().getImplementationVersion();
    long userCount = this.firestoreManager.getUserCount();
    long guildCount =
        event.getJDA().getShardManager() != null
            ? event.getJDA().getShardManager().getGuildCache().size()
            : event.getJDA().getGuildCache().size();
    ArrayList<MessageEmbed> messageEmbedList =
        this.helpEmbedFactory.create(name, version, userCount, guildCount);

    event.getHook().sendMessageEmbeds(messageEmbedList).queue();
  }

  private void queueAdditionalTestMessages(MessageChannel channel) {
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    TimerTask task =
        new TimerTask() {
          private int count = 1;

          public void run() {
            try {
              channel.sendMessageEmbeds(helpEmbedFactory.createTestMessage(count)).queue();
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
*/
