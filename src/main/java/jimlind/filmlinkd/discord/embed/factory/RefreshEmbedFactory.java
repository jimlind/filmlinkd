package jimlind.filmlinkd.discord.embed.factory;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import jimlind.filmlinkd.core.string.DisplayNameFormatter;
import jimlind.filmlinkd.core.string.UsernameFormatter;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LbMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

/** Builds a Discord embed to display result of a refresh request. */
public class RefreshEmbedFactory {
  private final DescriptionStringBuilder descriptionStringBuilder;
  private final EmbedBuilderFactory embedBuilderFactory;
  private final ImageUtils imageUtils;

  /**
   * Constructor for this class.
   *
   * @param descriptionStringBuilder Builds the description string truncating as necessary
   * @param embedBuilderFactory A factory for creating instances of the {@link EmbedBuilder} model
   * @param imageUtils Assists in finding optimal Letterboxd images
   */
  @Inject
  public RefreshEmbedFactory(
      DescriptionStringBuilder descriptionStringBuilder,
      EmbedBuilderFactory embedBuilderFactory,
      ImageUtils imageUtils) {
    this.descriptionStringBuilder = descriptionStringBuilder;
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

    String displayName = DisplayNameFormatter.format(member.displayName);
    String userName = UsernameFormatter.format(member.username);
    String description =
        String.format("I updated my display data for %s (%s).", displayName, userName);
    embedBuilder.setDescription(descriptionStringBuilder.setDescriptionText(description).build());
    embedBuilder.setThumbnail(imageUtils.getTallest(member.avatar));

    List<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
