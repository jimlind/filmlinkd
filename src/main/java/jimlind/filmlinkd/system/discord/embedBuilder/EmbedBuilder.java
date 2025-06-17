package jimlind.filmlinkd.system.discord.embedBuilder;

import java.awt.Color;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

public class EmbedBuilder extends net.dv8tion.jda.api.EmbedBuilder {
  @NotNull
  @Override
  public MessageEmbed build() {
    super.setColor(new Color(0xa700bd));
    return super.build();
  }
}
