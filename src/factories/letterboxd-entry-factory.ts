import LetterboxdEntry from '../models/letterboxd/letterboxd-entry.js';

export default class LetterboxdEntryFactory {
    /**
     * @param {import('./letterboxd/letterboxd-member-factory.mjs')} letterboxdMemberFactory
     */
    constructor(readonly letterboxdMemberFactory: any) {}

    /**
     * @param {Object} entryData
     * @returns LetterboxdEntry
     */
    buildFromObject(entryData: any) {
        const letterboxdEntry = new LetterboxdEntry();

        const tempDate = new Date(entryData.diaryDetails?.diaryDate).getTime();
        const date = !isNaN(tempDate) ? tempDate : null;

        const url = entryData.links.reduce(
            (previous: any, current: any) =>
                current.type == 'letterboxd' ? current.url : previous,
            '',
        );

        letterboxdEntry.id = entryData.id;
        letterboxdEntry.member = this.letterboxdMemberFactory.buildMemberSummaryFromObject(
            entryData.owner,
        );
        letterboxdEntry.filmName = entryData.film.name;
        letterboxdEntry.filmYear = entryData.film.releaseYear;
        letterboxdEntry.date = date as any;
        letterboxdEntry.rewatch = entryData.diaryDetails?.rewatch;
        letterboxdEntry.review = entryData.review?.text || '';
        letterboxdEntry.rating = entryData.rating || 0;
        letterboxdEntry.like = entryData.like;
        letterboxdEntry.url = url;

        return letterboxdEntry;
    }
}
