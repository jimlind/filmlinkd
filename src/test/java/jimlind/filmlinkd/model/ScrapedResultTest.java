package jimlind.filmlinkd.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

class ScrapedResultTest {
  @Mock private User user;
  @Mock private Message message;
  @Mock private Message.Entry entry;

  private ScrapedResult scrapedResult;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
    when(message.getEntry()).thenReturn(entry);
    scrapedResult = new ScrapedResult(message, user);
  }

  @Test
  @DisplayName("should return only override channel if available")
  void shouldReturnOnlyOverrideChannel() {
    when(message.getChannelId()).thenReturn("override-channel");
    when(message.hasChannelOverride()).thenReturn(true);
    assertEquals(List.of("override-channel"), scrapedResult.getChannelList());
  }

  @Test
  @DisplayName("should return all user channels if no override channel is present")
  void shouldReturnAllUserChannelsWithoutOverride() {
    when(message.getChannelId()).thenReturn(null);
    when(message.hasChannelOverride()).thenReturn(false);
    when(user.getChannelIdList()).thenReturn(List.of("user-channel"));
    assertEquals(List.of("user-channel"), scrapedResult.getChannelList());
  }
}
