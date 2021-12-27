const LetterboxdEntry = require('../models/letterboxd/letterboxd-entry');

class LetterboxdEntryFactory {
    /**
     * @param {import('./letterboxd/letterboxd-member-factory')} letterboxdMemberFactory
     */
    constructor(letterboxdMemberFactory) {
        this.letterboxdMemberFactory = letterboxdMemberFactory;
    }

    /**
     * @param {Object} entryData
     * @returns LetterboxdEntry
     */
    buildFromObject(entryData) {
        const letterboxdEntry = new LetterboxdEntry();

        const tempDate = new Date(entryData.diaryDetails?.diaryDate);
        const date = !isNaN(tempDate) ? tempDate : null;

        const url = entryData.links.reduce(
            (previous, current) => (current.type == 'letterboxd' ? current.url : previous),
            '',
        );

        letterboxdEntry.id = entryData.id;
        letterboxdEntry.member = this.letterboxdMemberFactory.buildFromObject(entryData.owner);
        letterboxdEntry.filmName = entryData.film.name;
        letterboxdEntry.filmYear = entryData.film.releaseYear;
        letterboxdEntry.date = date;
        letterboxdEntry.rewatch = entryData.diaryDetails?.rewatch;
        letterboxdEntry.review = entryData.review?.text || '';
        letterboxdEntry.rating = entryData.rating || 0;
        letterboxdEntry.like = entryData.like;
        letterboxdEntry.url = url;

        return letterboxdEntry;
    }
}

module.exports = LetterboxdEntryFactory;
