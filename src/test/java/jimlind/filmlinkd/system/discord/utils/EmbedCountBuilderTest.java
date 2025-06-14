package jimlind.filmlinkd.system.discord.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class EmbedCountBuilderTest {

  @Test
  void testBuild_lessThanThousand() {
    EmbedCountBuilder builder = new EmbedCountBuilder(123);
    assertEquals("123", builder.build());
  }

  @Test
  void testBuild_exactlyThousand() {
    EmbedCountBuilder builder = new EmbedCountBuilder(1000L);
    assertEquals("1K", builder.build());
  }

  @Test
  void testBuild_thousands() {
    EmbedCountBuilder builder = new EmbedCountBuilder(1234L);
    assertEquals("1.2K", builder.build());
  }

  @Test
  void testBuild_thousandsRoundingUp() {
    EmbedCountBuilder builder = new EmbedCountBuilder(1250L);
    assertEquals("1.3K", builder.build());
  }

  @Test
  void testBuild_thousandsNoDecimal() {
    EmbedCountBuilder builder = new EmbedCountBuilder(2000L);
    assertEquals("2K", builder.build());
  }

  @Test
  void testBuild_largeThousands() {
    EmbedCountBuilder builder = new EmbedCountBuilder(999999L);
    assertEquals("1000K", builder.build()); // This will round up to 1M
  }

  @Test
  void testBuild_largeThousandsJustBelowMillion() {
    EmbedCountBuilder builder = new EmbedCountBuilder(999499L);
    assertEquals("999.5K", builder.build());
  }

  @Test
  void testBuild_exactlyMillion() {
    EmbedCountBuilder builder = new EmbedCountBuilder(1000000L);
    assertEquals("1M", builder.build());
  }

  @Test
  void testBuild_millions() {
    EmbedCountBuilder builder = new EmbedCountBuilder(1234567L);
    assertEquals("1.2M", builder.build());
  }

  @Test
  void testBuild_millionsRoundingUp() {
    EmbedCountBuilder builder = new EmbedCountBuilder(1250000L);
    assertEquals("1.3M", builder.build());
  }

  @Test
  void testBuild_millionsNoDecimal() {
    EmbedCountBuilder builder = new EmbedCountBuilder(2000000L);
    assertEquals("2M", builder.build());
  }

  @Test
  void testBuild_largeMillions() {
    EmbedCountBuilder builder = new EmbedCountBuilder(999999999L);
    assertEquals("1000M", builder.build()); // This will round up to 1B
  }

  @Test
  void testBuild_largeMillionsJustBelowBillion() {
    EmbedCountBuilder builder = new EmbedCountBuilder(999499999L);
    assertEquals("999.5M", builder.build());
  }

  @Test
  void testBuild_exactlyBillion() {
    EmbedCountBuilder builder = new EmbedCountBuilder(1000000000L);
    assertEquals("1B", builder.build());
  }

  @Test
  void testBuild_billions() {
    EmbedCountBuilder builder = new EmbedCountBuilder(1234567890L);
    assertEquals("1.2B", builder.build());
  }

  @Test
  void testBuild_billionsRoundingUp() {
    EmbedCountBuilder builder = new EmbedCountBuilder(1250000000L);
    assertEquals("1.3B", builder.build());
  }

  @Test
  void testBuild_billionsNoDecimal() {
    EmbedCountBuilder builder = new EmbedCountBuilder(2000000000L);
    assertEquals("2B", builder.build());
  }

  @Test
  void testBuild_largeBillions() {
    EmbedCountBuilder builder = new EmbedCountBuilder(1234567890123L);
    assertEquals("1234.6B", builder.build());
  }

  @Test
  void testBuild_zero() {
    EmbedCountBuilder builder = new EmbedCountBuilder(0L);
    assertEquals("0", builder.build());
  }

  @Test
  void testBuild_negativeNumber() {
    // Current implementation treats negative numbers as positive for formatting
    EmbedCountBuilder builder = new EmbedCountBuilder(-1234L);
    assertEquals("-1234", builder.build());
  }

  @Test
  void testBuild_negativeNumberLessThanThousand() {
    EmbedCountBuilder builder = new EmbedCountBuilder(-123L);
    assertEquals("-123", builder.build());
  }
}
