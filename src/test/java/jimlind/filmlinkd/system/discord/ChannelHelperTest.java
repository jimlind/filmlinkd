package jimlind.filmlinkd.system.discord;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import jimlind.filmlinkd.system.discord.helper.ChannelHelper;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.SlashCommandInteraction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ChannelHelperTest {

  @Mock private Guild guild;
  @Mock private OptionMapping optionMapping;
  @Mock private SlashCommandInteraction interaction;
  @Mock private SlashCommandInteractionEvent event;
  @Mock private TextChannel textChannel;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(event.getInteraction()).thenReturn(interaction);
    when(interaction.getOption("channel")).thenReturn(optionMapping);
  }

  @Test
  void testGetChannelId_WithoutChannelOption() {
    when(event.getInteraction().getOption("channel")).thenReturn(null);
    when(event.getChannelId()).thenReturn("1234567890");

    assertEquals("1234567890", new ChannelHelper().getChannelId(event));
  }

  @Test
  void xtestGetChannelId_WithValidChannelMention() {
    when(event.getInteraction().getOption("channel")).thenReturn(optionMapping);
    when(optionMapping.getAsString()).thenReturn("<#9876543210>");

    assertEquals("9876543210", new ChannelHelper().getChannelId(event));
  }

  @Test
  void xtestGetChannelId_WithInvalidChannelMention() {
    when(event.getInteraction().getOption("channel")).thenReturn(optionMapping);
    when(optionMapping.getAsString()).thenReturn("invalid-channel");
    when(event.getGuild()).thenReturn(guild);
    when(guild.getTextChannelsByName("invalid-channel", true)).thenReturn(List.of());

    assertEquals("", new ChannelHelper().getChannelId(event));
  }

  @Test
  void xtestGetChannelId_WithGuildTextChannel() {
    when(event.getInteraction().getOption("channel")).thenReturn(optionMapping);
    when(optionMapping.getAsString()).thenReturn("general");
    when(event.getGuild()).thenReturn(guild);
    when(guild.getTextChannelsByName("general", true)).thenReturn(List.of(textChannel));
    when(textChannel.getId()).thenReturn("1357924680");

    assertEquals("1357924680", new ChannelHelper().getChannelId(event));
  }

  @Test
  void xtestGetChannelId_WithNullGuild() {
    when(event.getInteraction().getOption("channel")).thenReturn(optionMapping);
    when(optionMapping.getAsString()).thenReturn("random");
    when(event.getGuild()).thenReturn(null);

    assertEquals("", new ChannelHelper().getChannelId(event));
  }
}
