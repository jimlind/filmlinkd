/**
 * Letterboxd API Diary Details Object
 * https://api-docs.letterboxd.com/#/definitions/DiaryDetails
 */

export default class LetterboxdDiaryDetails {
    /** @property {string} diaryDate The date the film was watched, if specified, in ISO 8601 format, i.e. YYYY-MM-DD */
    diaryDate = '';
    /** @property {boolean} rewatch Will be true if the member has indicated (or it can be otherwise determined) that the member has seen the film prior to this date. */
    rewatch = false;
}
