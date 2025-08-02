package jimlind.filmlinkd.system.letterboxd.utils.extractor;

import jimlind.filmlinkd.system.letterboxd.model.LbDiaryDetails;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbReview;

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
   * Extract the review from a log entry.
   *
   * @param logEntry A Letterboxd log entry
   * @return The review
   */
  public static LbReview extractReview(LbLogEntry logEntry) {
    return logEntry.review;
  }
}
