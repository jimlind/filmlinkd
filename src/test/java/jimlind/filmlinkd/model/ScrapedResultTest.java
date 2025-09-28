package jimlind.filmlinkd.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

  @Nested
  @DisplayName("When entry is not newer")
  class WhenEntryIsNotNewer {
    @BeforeEach
    void setUp() {
      when(user.getMostRecentPrevious()).thenReturn("same-lid");
      when(entry.getLid()).thenReturn("same-lid");
    }

    @Test
    @DisplayName("should return only override channel to prevent duplicates")
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

  @Nested
  @DisplayName("When entry is newer")
  class WhenEntryIsNewer {
    @BeforeEach
    void setUp() {
      when(user.getMostRecentPrevious()).thenReturn("a");
      when(entry.getLid()).thenReturn("z");
    }

    @Test
    @DisplayName("should return only user channel list")
    void shouldReturnOnlyUserChannelList() {
      when(message.getChannelId()).thenReturn(null);
      when(message.hasChannelOverride()).thenReturn(false);
      when(user.getChannelIdList()).thenReturn(List.of("user-channel"));
      assertEquals(List.of("user-channel"), scrapedResult.getChannelList());
    }

    @Test
    @DisplayName("should return user channel list with override channel")
    void shouldReturnUserChannelListWithOverrideChannel() {
      when(message.getChannelId()).thenReturn("override-channel");
      when(message.hasChannelOverride()).thenReturn(true);
      when(user.getChannelIdList()).thenReturn(List.of("user-channel"));
      assertEquals(List.of("user-channel", "override-channel"), scrapedResult.getChannelList());
    }

    @Test
    @DisplayName("should return only user channel list when override duplicates")
    void shouldReturnUserChannelListWithoutOverrideChannel() {
      when(message.getChannelId()).thenReturn("same-channel");
      when(message.hasChannelOverride()).thenReturn(true);
      when(user.getChannelIdList()).thenReturn(List.of("same-channel"));
      assertEquals(List.of("same-channel"), scrapedResult.getChannelList());
    }
  }
}
