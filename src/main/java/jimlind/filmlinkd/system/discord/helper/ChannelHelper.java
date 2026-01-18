package jimlind.filmlinkd.system.discord.helper;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.inject.Singleton;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

/** A Collection of (actually one) methods that help translate channel data. */
@Singleton
public class ChannelHelper {
  /** Constructor for this class */
  @Inject
  public ChannelHelper() {}

  /**
   * Get the channel id as a string from the SlashCommandInteractionEvent. Returns an empty string
   * if nothing is available. There may be a channel value attached to the event option. That
   * channel value should come in as a properly created channel but might be a string. If there is
   * no option used then get the channel from the place that the event was created.
   *
   * @param event The SlashCommandInteractionEvent that contains the channel option or channel guild
   * @return The channel id as a string or empty string if not found
   */
  public String getChannelId(SlashCommandInteractionEvent event) {
    OptionMapping channelMap = event.getInteraction().getOption("channel");
    if (channelMap == null) {
      return event.getChannelId();
    }

    String channelString = channelMap.getAsString();
    Pattern pattern = Pattern.compile("^<#(\\d+)>$");
    Matcher matcher = pattern.matcher(channelString);
    if (matcher.find()) {
      return matcher.group(1);
    }

    Guild guild = event.getGuild();
    if (guild == null) {
      return "";
    }
    List<TextChannel> channelList = guild.getTextChannelsByName(channelString, true);
    if (channelList.isEmpty()) {
      return "";
    }

    return channelList.getFirst().getId();
  }
}
