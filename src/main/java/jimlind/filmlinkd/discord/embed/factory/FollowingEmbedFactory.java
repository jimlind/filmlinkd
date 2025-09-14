package jimlind.filmlinkd.discord.embed.factory;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import jimlind.filmlinkd.core.string.UsernameFormatter;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.model.User;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display information about all users following. */
public class FollowingEmbedFactory {
  private final EmbedBuilderFactory embedBuilderFactory;

  /**
   * Constructor for this class.
   *
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model
   */
  @Inject
  FollowingEmbedFactory(EmbedBuilderFactory embedBuilderFactory) {
    this.embedBuilderFactory = embedBuilderFactory;
  }

  /**
   * Builds the embed.
   *
   * @param userMap Map fetched from the database containing username and display name
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains as many embed objects as are needed to contain all users.
   */
  public List<MessageEmbed> create(Map<String, User> userMap) {
    List<MessageEmbed> embedList = new ArrayList<>();

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
              UsernameFormatter.format(user.userName), user.letterboxdId, user.letterboxdId);

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
