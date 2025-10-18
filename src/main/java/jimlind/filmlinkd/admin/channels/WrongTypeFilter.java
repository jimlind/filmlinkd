package jimlind.filmlinkd.admin.channels;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel;
import net.dv8tion.jda.api.sharding.ShardManager;

/** Filter out channels that are wrong type. */
public class WrongTypeFilter {
  /**
   * Returns the list of channels that are wrong type.
   *
   * @param globalShardManager Shard manager used to look up channels
   * @param input A complete least of all possible channels
   * @return The list of channels that are wrong type
   */
  public List<String> filter(ShardManager globalShardManager, List<String> input) {
    try (PrintWriter out = new PrintWriter(System.out, true)) {
      List<String> wrongTypeChannels = new ArrayList<>(input);
      for (int i = 0; i < 10; i++) {
        out.println(wrongTypeChannels.size() + " possible wrong type channels");
        Iterator<String> wrongTypeIterator = wrongTypeChannels.iterator();
        while (wrongTypeIterator.hasNext()) {
          String channelId = wrongTypeIterator.next();
          GuildMessageChannel channel =
              globalShardManager.getChannelById(GuildMessageChannel.class, channelId);
          // If the channel is not null remove it from the list.
          // The list should only contain wrong type channels.
          if (channel != null) {
            wrongTypeIterator.remove();
          }
        }
        try {
          Thread.sleep(2000);
        } catch (InterruptedException ignore) {
          return List.of();
        }
      }
      return wrongTypeChannels;
    }
  }
}
