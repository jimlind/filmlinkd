package jimlind.filmlinkd.system.discord.embedBuilder;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.discord.stringBuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringBuilder.UserStringBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class FollowingEmbedBuilder {
  private final EmbedBuilderFactory embedBuilderFactory;
  private TreeMap<String, User> userMap = new TreeMap<>();

  @Inject
  FollowingEmbedBuilder(EmbedBuilderFactory embedBuilderFactory) {
    this.embedBuilderFactory = embedBuilderFactory;
  }

  public FollowingEmbedBuilder setUserMap(TreeMap<String, User> userMap) {
    this.userMap = userMap;
    return this;
  }

  public ArrayList<MessageEmbed> build() {
    ArrayList<MessageEmbed> embedList = new ArrayList<>();

    String description =
        new DescriptionStringBuilder()
            .setDescriptionText("Here are the accounts I'm following...")
            .build();
    embedList.add(embedBuilderFactory.create().setDescription(description).build());

    String resultString = "";
    for (Map.Entry<String, User> entry : userMap.entrySet()) {
      User user = entry.getValue();
      String userDisplay =
          String.format(
              "• %s [%s](https://boxd.it/%s)\n",
              new UserStringBuilder().setUserName(user.userName).build(),
              user.letterboxdId,
              user.letterboxdId);

      // Instead of checking the string length against what's known in the EmbedDescription it is
      // slightly more accurate to try setting EmbedDescription and reacting when the length
      // exceptions are thrown.
      String nextString = resultString + userDisplay;
      try {
        embedBuilderFactory.create().setDescription(nextString);
        // Updating resultString now only happens if setDescription doesn't fail. Logic like this is
        // "clever" and should usually be avoided for readability.
        resultString = nextString;
      } catch (IllegalArgumentException e) {
        embedList.add(embedBuilderFactory.create().setDescription(resultString).build());
        resultString = userDisplay;
      }
    }
    // If there is anything left over add that as a message as well.
    if (!resultString.isBlank()) {
      embedList.add(embedBuilderFactory.create().setDescription(resultString).build());
    }

    return embedList;
  }
}
