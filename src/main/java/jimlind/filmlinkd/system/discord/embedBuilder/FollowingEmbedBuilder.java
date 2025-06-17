package jimlind.filmlinkd.system.discord.embedBuilder;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.discord.utils.EmbedDescriptionBuilder;
import jimlind.filmlinkd.system.discord.utils.EmbedUserBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class FollowingEmbedBuilder {
  public ArrayList<MessageEmbed> buildEmbedList(TreeMap<String, User> userMap) {
    ArrayList<MessageEmbed> embedList = new ArrayList<>();

    String description =
        new EmbedDescriptionBuilder("Here are the accounts I'm following...").build();
    embedList.add(new EmbedBuilder().setDescription(description).build());

    String resultString = "";
    for (Map.Entry<String, User> entry : userMap.entrySet()) {
      User user = entry.getValue();
      String userDisplay =
          String.format(
              "• %s [%s](https://boxd.it/%s)\n",
              new EmbedUserBuilder(user.userName).build(), user.letterboxdId, user.letterboxdId);

      // Instead of checking the string length against what's known in the EmbedDescription it is
      // slightly more accurate to try setting EmbedDescription and reacting when the length
      // exceptions are thrown.
      String nextString = resultString + userDisplay;
      try {
        new EmbedBuilder().setDescription(nextString);
        // Updating resultString now only happens if setDescription doesn't fail. Logic like this is
        // "clever" and should usually be avoided for readability.
        resultString = nextString;
      } catch (IllegalArgumentException e) {
        embedList.add(new EmbedBuilder().setDescription(resultString).build());
        resultString = userDisplay;
      }
    }
    // If there is anything left over add that as a message as well.
    if (!resultString.isBlank()) {
      embedList.add(new EmbedBuilder().setDescription(resultString).build());
    }

    return embedList;
  }
}
