package jimlind.filmlinkd.admin.channels;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.sharding.ShardManager;

/** Filter out channels that are null. */
public class NullFilter {
  /**
   * Returns the list of channels that are null.
   *
   * @param globalShardManager Shard manager used to look up channels
   * @param input A complete least of all possible channels
   * @return The list of channels that are null
   */
  public List<String> filter(ShardManager globalShardManager, List<String> input) {
    PrintWriter out = new PrintWriter(System.out, true);
    List<String> nullChannels = new ArrayList<>(input);
    for (int i = 0; i < 10; i++) {
      out.println(nullChannels.size() + " possible null channels");
      Iterator<String> nullIterator = nullChannels.iterator();
      while (nullIterator.hasNext()) {
        String channelId = nullIterator.next();
        GuildChannel channel = globalShardManager.getGuildChannelById(channelId);
        // If the channel is not null remove it from the list.
        // The list should only contain null channels.
        if (channel != null) {
          nullIterator.remove();
        }
      }
      try {
        Thread.sleep(2000);
      } catch (InterruptedException ignore) {
        // Do nothing
      }
    }
    return nullChannels;
  }
}
