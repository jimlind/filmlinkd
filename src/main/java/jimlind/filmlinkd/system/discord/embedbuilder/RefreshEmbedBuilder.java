package jimlind.filmlinkd.system.discord.embedbuilder;

import com.google.inject.Inject;
import java.util.ArrayList;
import jimlind.filmlinkd.factory.EmbedBuilderFactory;
import jimlind.filmlinkd.system.discord.stringbuilder.DescriptionStringBuilder;
import jimlind.filmlinkd.system.discord.stringbuilder.UserStringBuilder;
import jimlind.filmlinkd.system.letterboxd.model.LBMember;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;

public class RefreshEmbedBuilder {
  private final DescriptionStringBuilder descriptionStringBuilder;
  private final net.dv8tion.jda.api.EmbedBuilder embedBuilder;
  private final ImageUtils imageUtils;
  private final UserStringBuilder userStringBuilder;
  private LBMember member = null;

  @Inject
  public RefreshEmbedBuilder(
      DescriptionStringBuilder descriptionStringBuilder,
      EmbedBuilderFactory embedBuilderFactory,
      ImageUtils imageUtils,
      UserStringBuilder userStringBuilder) {
    this.descriptionStringBuilder = descriptionStringBuilder;
    this.imageUtils = imageUtils;
    this.userStringBuilder = userStringBuilder;
    embedBuilder = embedBuilderFactory.create();
  }

  public RefreshEmbedBuilder setMember(LBMember member) {
    this.member = member;
    return this;
  }

  public ArrayList<MessageEmbed> build() {
    if (member == null) {
      return new ArrayList<>();
    }

    String userName = userStringBuilder.setUserName(member.username).build();
    String description =
        String.format("I updated my display data for %s (%s).", member.displayName, userName);
    embedBuilder.setDescription(descriptionStringBuilder.setDescriptionText(description).build());
    embedBuilder.setThumbnail(imageUtils.getTallest(member.avatar));

    ArrayList<MessageEmbed> collection = new ArrayList<>();
    collection.add(embedBuilder.build());

    return collection;
  }
}
