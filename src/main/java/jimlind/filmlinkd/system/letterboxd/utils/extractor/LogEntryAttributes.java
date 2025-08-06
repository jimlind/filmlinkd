package jimlind.filmlinkd.system.letterboxd.utils.extractor;

import jimlind.filmlinkd.system.letterboxd.model.LbDiaryDetails;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbReview;
import org.jetbrains.annotations.Nullable;

/** Extract attributes from the LbLogEntry Model. */
public final class LogEntryAttributes {
  /** Utility constructor. */
  private LogEntryAttributes() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }

  /**
   * Extract the diary details from a log entry.
   *
   * @param logEntry A Letterboxd log entry
   * @return The diary details
   */
  public static LbDiaryDetails extractDiaryDetails(LbLogEntry logEntry) {
    return logEntry.diaryDetails;
  }

  /**
   * Extract the film summary from a log entry.
   *
   * @param logEntry A Letterboxd log entry
   * @return The film summary
   */
  public static LbFilmSummary extractFilm(LbLogEntry logEntry) {
    return logEntry.film;
  }

  /**
   * Extract the owner from a log entry.
   *
   * @param logEntry A Letterboxd log entry
   * @return The owner
   */
  public static LbMemberSummary extractOwner(LbLogEntry logEntry) {
    return logEntry.owner;
  }

  /**
   * Extract the spoiler status from a log entry.
   *
   * @param logEntry A Letterboxd log entry
   * @return If the log entry has spoilers
   */
  public static boolean extractSpoilerStatus(LbLogEntry logEntry) {
    LbReview review = getLbReview(logEntry);
    return review != null && review.isContainsSpoilers();
  }

  /**
   * Extract the text of the review from a log entry.
   *
   * @param logEntry A Letterboxd log entry
   * @return The review string
   */
  public static String extractReviewText(LbLogEntry logEntry) {
    LbReview review = getLbReview(logEntry);
    if (review == null) {
      return "";
    }
    if (review.getText().isBlank()) {
      return "";
    }
    return review.toString();
  }

  @Nullable
  private static LbReview getLbReview(LbLogEntry logEntry) {
    return logEntry.review;
  }
}
