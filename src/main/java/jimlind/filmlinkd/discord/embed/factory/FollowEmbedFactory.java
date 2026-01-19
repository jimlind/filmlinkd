package jimlind.filmlinkd.discord.embed.factory;

import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import jimlind.filmlinkd.core.string.DisplayNameFormatter;
import jimlind.filmlinkd.core.string.UsernameFormatter;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display the result of a follow request. */
@Singleton
public class FollowEmbedFactory {
  private final EmbedBuilderFactory embedBuilderFactory;
  private final ImageUtils imageUtils;

  /**
   * Constructor for this class.
   *
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model
   * @param imageUtils Assists in finding optimal Letterboxd images
   */
  @Inject
  public FollowEmbedFactory(EmbedBuilderFactory embedBuilderFactory, ImageUtils imageUtils) {
    this.embedBuilderFactory = embedBuilderFactory;
    this.imageUtils = imageUtils;
  }

  /**
   * Builds the embed.
   *
   * @param member Member model from Letterboxd API
   * @return A fully constructed list of embeds that are ready to be sent to users. Here the list
   *     contains only one embed.
   */
  public List<MessageEmbed> create(LbMember member) {
    EmbedBuilder embedBuilder = embedBuilderFactory.create();

    String userName = UsernameFormatter.format(member.username);
    String displayName = DisplayNameFormatter.format(member.displayName);
    String description = String.format("I am now following %s (%s).\n", displayName, userName);
    description +=
        "New diary entries will be displayed when they are available. Please be patient.";
    embedBuilder.setDescription(
        new DescriptionStringBuilder().setDescriptionText(description).build());
    embedBuilder.setThumbnail(imageUtils.getTallest(member.avatar));

    List<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
