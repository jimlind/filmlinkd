package jimlind.filmlinkd.system.letterboxd.utils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import lombok.extern.slf4j.Slf4j;

/** Utilities to translate Letterboxd date strings to other formats. */
@Slf4j
public class DateUtils {

  /**
   * Translates a Letterboxd date string to a timestamp in milliseconds.
   *
   * @param dateString A Letterboxd date string
   * @return A timestamp in milliseconds
   */
  public long toMilliseconds(String dateString) {
    if (dateString == null || dateString.isEmpty()) {
      return 0L;
    }

    // TODO: This used to have an unchecked try/catch wrapper
    try {
      // Try parsing as LocalDate first
      LocalDate localDate = LocalDate.parse(dateString);
      return localDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli();
    } catch (DateTimeParseException e1) {
      try {
        // Try parsing as ZonedDateTime
        ZonedDateTime zonedDate = ZonedDateTime.parse(dateString);
        return zonedDate.toInstant().toEpochMilli();
      } catch (DateTimeParseException e2) {
        log.atWarn().setMessage("Failed to parse date string: {}").addArgument(dateString).log();
      }
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
