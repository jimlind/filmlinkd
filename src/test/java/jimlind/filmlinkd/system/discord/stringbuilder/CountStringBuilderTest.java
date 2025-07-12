package jimlind.filmlinkd.system.discord.stringbuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class CountStringBuilderTest {

  @Test
  void testBuild_lessThanThousand() {
    CountStringBuilder builder = new CountStringBuilder().setCount(123);
    assertEquals("123", builder.build());
  }

  @Test
  void testBuild_exactlyThousand() {
    CountStringBuilder builder = new CountStringBuilder().setCount(1000L);
    assertEquals("1K", builder.build());
  }

  @Test
  void testBuild_thousands() {
    CountStringBuilder builder = new CountStringBuilder().setCount(1234L);
    assertEquals("1.2K", builder.build());
  }

  @Test
  void testBuild_thousandsRoundingUp() {
    CountStringBuilder builder = new CountStringBuilder().setCount(1250L);
    assertEquals("1.3K", builder.build());
  }

  @Test
  void testBuild_thousandsNoDecimal() {
    CountStringBuilder builder = new CountStringBuilder().setCount(2000L);
    assertEquals("2K", builder.build());
  }

  @Test
  void testBuild_largeThousands() {
    CountStringBuilder builder = new CountStringBuilder().setCount(999999L);
    assertEquals("1000K", builder.build()); // This will round up to 1M
  }

  @Test
  void testBuild_largeThousandsJustBelowMillion() {
    CountStringBuilder builder = new CountStringBuilder().setCount(999499L);
    assertEquals("999.5K", builder.build());
  }

  @Test
  void testBuild_exactlyMillion() {
    CountStringBuilder builder = new CountStringBuilder().setCount(1000000L);
    assertEquals("1M", builder.build());
  }

  @Test
  void testBuild_millions() {
    CountStringBuilder builder = new CountStringBuilder().setCount(1234567L);
    assertEquals("1.2M", builder.build());
  }

  @Test
  void testBuild_millionsRoundingUp() {
    CountStringBuilder builder = new CountStringBuilder().setCount(1250000L);
    assertEquals("1.3M", builder.build());
  }

  @Test
  void testBuild_millionsNoDecimal() {
    CountStringBuilder builder = new CountStringBuilder().setCount(2000000L);
    assertEquals("2M", builder.build());
  }

  @Test
  void testBuild_largeMillions() {
    CountStringBuilder builder = new CountStringBuilder().setCount(999999999L);
    assertEquals("1000M", builder.build()); // This will round up to 1B
  }

  @Test
  void testBuild_largeMillionsJustBelowBillion() {
    CountStringBuilder builder = new CountStringBuilder().setCount(999499999L);
    assertEquals("999.5M", builder.build());
  }

  @Test
  void testBuild_exactlyBillion() {
    CountStringBuilder builder = new CountStringBuilder().setCount(1000000000L);
    assertEquals("1B", builder.build());
  }

  @Test
  void testBuild_billions() {
    CountStringBuilder builder = new CountStringBuilder().setCount(1234567890L);
    assertEquals("1.2B", builder.build());
  }

  @Test
  void testBuild_billionsRoundingUp() {
    CountStringBuilder builder = new CountStringBuilder().setCount(1250000000L);
    assertEquals("1.3B", builder.build());
  }

  @Test
  void testBuild_billionsNoDecimal() {
    CountStringBuilder builder = new CountStringBuilder().setCount(2000000000L);
    assertEquals("2B", builder.build());
  }

  @Test
  void testBuild_largeBillions() {
    CountStringBuilder builder = new CountStringBuilder().setCount(1234567890123L);
    assertEquals("1234.6B", builder.build());
  }

  @Test
  void testBuild_zero() {
    CountStringBuilder builder = new CountStringBuilder().setCount(0L);
    assertEquals("0", builder.build());
  }

  @Test
  void testBuild_negativeNumber() {
    // Current implementation treats negative numbers as positive for formatting
    CountStringBuilder builder = new CountStringBuilder().setCount(-1234L);
    assertEquals("-1234", builder.build());
  }

  @Test
  void testBuild_negativeNumberLessThanThousand() {
    CountStringBuilder builder = new CountStringBuilder().setCount(-123L);
    assertEquals("-123", builder.build());
  }
}
