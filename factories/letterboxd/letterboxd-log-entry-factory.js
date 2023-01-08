const LetterboxdLogEntry = require('../../models/letterboxd/letterboxd-log-entry');

class LetterboxdLogEntryFactory {
    /**
     * @param {import('./letterboxd-film-factory')} letterboxdFilmFactory
     * @param {import('./letterboxd-image-factory')} letterboxdImageFactory
     * @param {import('./letterboxd-link-factory')} letterboxdLinkFactory
     * @param {import('./letterboxd-member-factory')} letterboxdMemberFactory
     * @param {import('./letterboxd-tag-factory')} letterboxdTagFactory
     */
    constructor(
        letterboxdFilmFactory,
        letterboxdImageFactory,
        letterboxdLinkFactory,
        letterboxdMemberFactory,
        letterboxdTagFactory,
    ) {
        this.letterboxdFilmFactory = letterboxdFilmFactory;
        this.letterboxdImageFactory = letterboxdImageFactory;
        this.letterboxdLinkFactory = letterboxdLinkFactory;
        this.letterboxdMemberFactory = letterboxdMemberFactory;
        this.letterboxdTagFactory = letterboxdTagFactory;
    }

    /**
     * @param {Object} logEntryData
     * @returns LetterboxdCountry
     */
    buildLogEntryFromObject(logEntryData) {
        const letterboxdLogEntry = new LetterboxdLogEntry();

        letterboxdLogEntry.id = logEntryData.id || '';
        letterboxdLogEntry.name = logEntryData.name || '';
        letterboxdLogEntry.owner = this.letterboxdMemberFactory.buildMemberSummaryFromObject(
            logEntryData.owner,
        );
        letterboxdLogEntry.film = this.letterboxdFilmFactory.buildFilmSummaryFromObject(
            logEntryData.film,
        );
        /** @property {import('./letterboxd-diary-details)} diaryDetails Details about the log entry, if present */
        letterboxdLogEntry.diaryDetails = logEntryData.diaryDetails;
        /** @property {import('./letterboxd-review')} review Review details for the log entry, if present. */
        letterboxdLogEntry.review = logEntryData.review;
        letterboxdLogEntry.tags = logEntryData.tags || [];
        letterboxdLogEntry.tags2 = (logEntryData.tags2 || []).map((tag) =>
            this.letterboxdTagFactory.buildTagFromObject(tag),
        );
        letterboxdLogEntry.whenCreated = logEntryData.whenCreated || '';
        letterboxdLogEntry.whenUpdated = logEntryData.whenUpdated || '';
        letterboxdLogEntry.rating = logEntryData.rating || 0;
        letterboxdLogEntry.like = logEntryData.like || false;
        letterboxdLogEntry.commentable = logEntryData.commentable || false;
        letterboxdLogEntry.commentPolicy = logEntryData.commentPolicy || '';
        letterboxdLogEntry.links = (logEntryData.links || []).map((link) =>
            this.letterboxdLinkFactory.buildLinkFromObject(link),
        );
        letterboxdLogEntry.backdrop = this.letterboxdImageFactory.buildImageFromObject(
            logEntryData.backdrop,
        );
        letterboxdLogEntry.backdropFocalPoint = logEntryData.backdropFocalPoint || 0;

        return letterboxdLogEntry;
    }
}

module.exports = LetterboxdLogEntryFactory;
