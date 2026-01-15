package jimlind.filmlinkd.admin.channels;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.sharding.ShardManager;

/** Filter out channels that have wrong permissions. */
public class WrongPermissionsFilter {

  private static ChannelType extractType(GuildChannel channel) {
    return channel.getType();
  }

  /**
   * Returns the list of channels that have wrong permissions.
   *
   * @param globalShardManager Shard manager used to look up channels
   * @param input A complete least of all possible channels
   * @return The list of channels that have wrong permissions
   */
  public List<String> filter(ShardManager globalShardManager, List<String> input) {
    try (PrintWriter out =
        new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8), true)) {
      List<String> wrongPermissionsChannels = new ArrayList<>(input);
      for (int i = 0; i < 10; i++) {
        out.println(wrongPermissionsChannels.size() + " possible wrong permissions channels");
        Iterator<String> wrongPermissionsIterator = wrongPermissionsChannels.iterator();
        while (wrongPermissionsIterator.hasNext()) {
          String channelId = wrongPermissionsIterator.next();
          GuildChannel channel = globalShardManager.getGuildChannelById(channelId);
          Member member = channel.getGuild().getSelfMember();
          boolean viewChannelEnabled = member.hasPermission(channel, Permission.VIEW_CHANNEL);
          boolean sendMessageEnabled =
              extractType(channel).isThread()
                  ? member.hasPermission(channel, Permission.MESSAGE_SEND_IN_THREADS)
                  : member.hasPermission(channel, Permission.MESSAGE_SEND);
          boolean embedLinkEnabled = member.hasPermission(channel, Permission.MESSAGE_EMBED_LINKS);
          // If the channel has proper permissions remove it from the list.
          // The list should only contain wrong permissions channels.
          if (viewChannelEnabled && sendMessageEnabled && embedLinkEnabled) {
            wrongPermissionsIterator.remove();
          }
        }
        try {
          Thread.sleep(2000);
        } catch (InterruptedException ignore) {
          return List.of();
        }
      }
      return wrongPermissionsChannels;
    }
  }
}
