package jimlind.filmlinkd.system.discord;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

public class ChannelHelper {
  public static String getChannelId(SlashCommandInteractionEvent event) {
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

    return channelList.get(0).getId();
  }
}
