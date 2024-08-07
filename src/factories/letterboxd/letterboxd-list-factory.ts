import LetterboxdListEntrySummary from '../../models/letterboxd/letterboxd-list-entry-summary.js';
import LetterboxdListIdentifier from '../../models/letterboxd/letterboxd-list-identifier.js';
import LetterboxdListSummary from '../../models/letterboxd/letterboxd-list-summary.js';

export default class LetterboxdListFactory {
    /**
     * @param {import('./letterboxd-film-factory.mjs')} letterboxdFilmFactory
     * @param {import('./letterboxd-member-factory.mjs')} letterboxdMemberFactory
     */
    constructor(readonly letterboxdFilmFactory: any, readonly letterboxdMemberFactory: any) {}

    /**
     * @param {Object} listEntrySummary
     * @returns LetterboxdListEntrySummary;
     */
    buildListEntrySummaryFromObject(listEntrySummaryData: any) {
        const listEntrySummary = new LetterboxdListEntrySummary();

        listEntrySummary.rank = listEntrySummaryData.rank || 0;
        listEntrySummary.film = this.letterboxdFilmFactory.buildFilmSummaryFromObject(
            listEntrySummaryData.film,
        );

        return listEntrySummary;
    }

    /**
     * @param {Object} listIdentifierData
     * @returns LetterboxdListIdentifier | null;
     */
    buildListIdentifierFromObject(listIdentifierData: any) {
        if (!listIdentifierData?.id || false) {
            return null;
        }

        const letterboxdListIdentifier = new LetterboxdListIdentifier();
        letterboxdListIdentifier.id = listIdentifierData.id;

        return letterboxdListIdentifier;
    }

    /**
     * @param {Object} listSummaryData
     * @returns LetterboxdListSummary
     */
    buildListSummaryFromObject(listSummaryData: any) {
        const letterboxdListSummary = new LetterboxdListSummary();

        letterboxdListSummary.id = listSummaryData.id || '';
        letterboxdListSummary.name = listSummaryData.name || '';
        letterboxdListSummary.filmCount = listSummaryData.filmCount || 0;
        letterboxdListSummary.published = listSummaryData.published || true;
        letterboxdListSummary.ranked = listSummaryData.ranked || false;
        letterboxdListSummary.descriptionLbml = listSummaryData.descriptionLbml || '';
        letterboxdListSummary.descriptionTruncated = listSummaryData.descriptionTruncated || false;
        letterboxdListSummary.owner = this.letterboxdMemberFactory.buildMemberSummaryFromObject(
            listSummaryData.owner,
        );
        letterboxdListSummary.clonedFrom = this.buildListIdentifierFromObject(
            listSummaryData.clonedFrom,
        ) as any;
        letterboxdListSummary.previewEntries = listSummaryData.previewEntries.map(
            (previewEntry: any) => this.buildListEntrySummaryFromObject(previewEntry),
        );
        // TODO: Set this properly when/if needed
        letterboxdListSummary.entriesOfNote = [];
        letterboxdListSummary.description = listSummaryData.description || '';

        return letterboxdListSummary;
    }
}
