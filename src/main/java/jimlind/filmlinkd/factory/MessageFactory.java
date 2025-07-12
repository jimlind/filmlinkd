package jimlind.filmlinkd.factory;

import com.google.inject.Inject;
import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
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
  public Message createFromLogEntry(LBLogEntry logEntry, Message.PublishSource publishSource) {
    Message message = new Message();

    message.entry = new Message.Entry();
    message.entry.lid = logEntry.id;
    message.entry.userName = logEntry.owner.username;
    message.entry.userLid = logEntry.owner.id;
    message.entry.type =
        (logEntry.review != null && !logEntry.review.text.isBlank())
            ? Message.Type.review
            : Message.Type.watch;
    message.entry.link = linkUtils.getLetterboxd(logEntry.links);
    message.entry.publishedDate = dateUtils.toMilliseconds(logEntry.whenCreated);
    message.entry.filmTitle = logEntry.film.name;
    message.entry.filmYear = logEntry.film.releaseYear;
    message.entry.watchedDate =
        dateUtils.toMilliseconds(
            logEntry.diaryDetails != null ? logEntry.diaryDetails.diaryDate : "");
    message.entry.image = imageUtils.getTallest(logEntry.film.poster);
    message.entry.starCount = logEntry.rating;
    message.entry.rewatch = logEntry.diaryDetails != null && logEntry.diaryDetails.rewatch;
    message.entry.liked = logEntry.like;
    message.entry.containsSpoilers = logEntry.review != null && logEntry.review.containsSpoilers;
    message.entry.adult = logEntry.film.adult;
    message.entry.review = logEntry.review != null ? logEntry.review.text : "";
    message.entry.updatedDate = dateUtils.toMilliseconds(logEntry.whenUpdated);
    message.entry.publishSource = publishSource;

    return message;
  }
}
