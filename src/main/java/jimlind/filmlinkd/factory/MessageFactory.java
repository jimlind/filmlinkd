package jimlind.filmlinkd.factory;

import com.google.inject.Inject;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.letterboxd.model.LbDiaryDetails;
import jimlind.filmlinkd.system.letterboxd.model.LbFilmSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbLogEntry;
import jimlind.filmlinkd.system.letterboxd.model.LbMemberSummary;
import jimlind.filmlinkd.system.letterboxd.model.LbReview;
import jimlind.filmlinkd.system.letterboxd.utils.DateUtils;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import jimlind.filmlinkd.system.letterboxd.utils.LinkUtils;

/** A factory for creating instances of the {@link Message} model. */
public class MessageFactory {
  private final DateUtils dateUtils;
  private final LinkUtils linkUtils;
  private final ImageUtils imageUtils;

  /**
   * Constructor for the {@link MessageFactory}.
   *
   * @param dateUtils Helpers for processing Letterboxd dates
   * @param linkUtils Helpers for processing Letterboxd URLs
   * @param imageUtils Helpers for processing Letterboxd images
   */
  @Inject
  MessageFactory(DateUtils dateUtils, LinkUtils linkUtils, ImageUtils imageUtils) {
    this.dateUtils = dateUtils;
    this.linkUtils = linkUtils;
    this.imageUtils = imageUtils;
  }

  /**
   * Create our proper {@link Message} object from two data sources.
   *
   * @param logEntry A model from the Letterboxd API response
   * @param publishSource The source of the message creation (scraping, etc)
   * @return A {@link Message} object
   */
  public Message createFromLogEntry(LbLogEntry logEntry, Message.PublishSource publishSource) {
    Message.Entry entry = new Message.Entry();
    entry.setLid(logEntry.id);

    LbMemberSummary owner = getOwner(logEntry);
    entry.setUserName(owner.username);
    entry.setUserLid(owner.id);

    LbReview review = getReview(logEntry);
    boolean hasValidReview = review != null && !review.text.isBlank();
    entry.setType(hasValidReview ? Message.Type.review : Message.Type.watch);

    entry.setLink(linkUtils.getLetterboxd(logEntry.links));
    entry.setPublishedDate(dateUtils.toMilliseconds(logEntry.whenCreated));

    LbFilmSummary film = getFilm(logEntry);
    entry.setFilmTitle(film.name);
    entry.setFilmYear(film.releaseYear);

    LbDiaryDetails diaryDetails = getDiaryDetails(logEntry);
    String diaryDate = (diaryDetails != null) ? diaryDetails.diaryDate : "";
    entry.setWatchedDate(dateUtils.toMilliseconds(diaryDate));

    entry.setImage(imageUtils.getTallest(film.poster));
    entry.setStarCount(logEntry.rating);
    entry.setRewatch(diaryDetails != null && diaryDetails.rewatch);
    entry.setLiked(logEntry.like);
    entry.setContainsSpoilers(review != null && review.containsSpoilers);
    entry.setAdult(film.adult);
    entry.setReview(review != null ? review.text : "");
    entry.setUpdatedDate(dateUtils.toMilliseconds(logEntry.whenUpdated));
    entry.setPublishSource(publishSource);

    Message message = new Message();
    message.setEntry(entry);

    return message;
  }

  private LbDiaryDetails getDiaryDetails(LbLogEntry logEntry) {
    return logEntry.diaryDetails;
  }

  private LbFilmSummary getFilm(LbLogEntry logEntry) {
    return logEntry.film;
  }

  private LbMemberSummary getOwner(LbLogEntry logEntry) {
    return logEntry.owner;
  }

  private LbReview getReview(LbLogEntry logEntry) {
    return logEntry.review;
  }
}
