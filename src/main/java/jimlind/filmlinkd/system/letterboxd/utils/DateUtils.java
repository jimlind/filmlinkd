package jimlind.filmlinkd.system.letterboxd.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Utilities to translate Letterboxd date strings to other formats. */
public class DateUtils {

  /**
   * Translates a Letterboxd date string to a timestamp in milliseconds.
   *
   * @param dateString A Letterboxd date string
   * @return A timestamp in milliseconds
   */
  public long toMilliseconds(String dateString) {
    try {
      LocalDate date = LocalDate.parse(dateString);
      return date.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    } catch (Exception e) {
      // Do nothing
    }

    try {
      ZonedDateTime date = ZonedDateTime.parse(dateString);
      return date.toInstant().toEpochMilli();
    } catch (Exception e) {
      // Do nothing
    }

    return 0L;
  }

  /**
   * Translates a Letterboxd date string to a human-readable date string.
   *
   * @param dateString A Letterboxd date string
   * @return A human-readable date string
   */
  public String toPattern(String dateString) {
    long timestamp = toMilliseconds(dateString);
    Instant instant = Instant.ofEpochMilli(timestamp);
    ZonedDateTime zonedDateTime = instant.atZone(ZoneOffset.UTC);

    String pattern =
        Instant.now().toEpochMilli() - timestamp < 5000000000L ? "MMM dd" : "MMM dd uuu";

    return zonedDateTime.format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
  }
}
