package jimlind.filmlinkd.factory;

import jimlind.filmlinkd.model.Message;
import jimlind.filmlinkd.system.letterboxd.model.LBLogEntry;
import jimlind.filmlinkd.system.letterboxd.utils.DateUtils;
import jimlind.filmlinkd.system.letterboxd.utils.ImageUtils;
import jimlind.filmlinkd.system.letterboxd.utils.LinkUtils;

public class MessageFactory {
  private final DateUtils dateUtils;
  private final LinkUtils linkUtils;
  private final ImageUtils imageUtils;

  MessageFactory(DateUtils dateUtils, LinkUtils linkUtils, ImageUtils imageUtils) {
    this.dateUtils = dateUtils;
    this.linkUtils = linkUtils;
    this.imageUtils = imageUtils;
  }

  public Message createFromLogEntry(LBLogEntry logEntry, Message.PublishSource publishSource) {
    Message.Type type =
        (logEntry.review != null && !logEntry.review.text.isBlank())
            ? Message.Type.review
            : Message.Type.watch;
    String link = linkUtils.getLetterboxd(logEntry.links);

    Message message = new Message();

    message.entry = new Message.Entry();
    message.entry.lid = logEntry.id;
    message.entry.userName = logEntry.owner.username;
    message.entry.userLid = logEntry.owner.id;
    message.entry.type = type;
    message.entry.link = link;
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
