import LetterboxdPronoun from '../../models/letterboxd/letterboxd-pronoun.js';

export default class LetterboxdPronounFactory {
    /**
     * @param {Object} pronounData
     * @returns LetterboxdPronoun
     */
    buildPronounFromObject(pronounData: any) {
        const letterboxdPronoun = new LetterboxdPronoun();

        letterboxdPronoun.id = pronounData.id || '';
        letterboxdPronoun.label = pronounData.label || '';
        letterboxdPronoun.subjectPronoun = pronounData.subjectPronoun || '';
        letterboxdPronoun.objectPronoun = pronounData.objectPronoun || '';
        letterboxdPronoun.possessivePronoun = pronounData.possessivePronoun || '';
        letterboxdPronoun.possessiveAdjective = pronounData.possessiveAdjective || '';
        letterboxdPronoun.reflexive = pronounData.reflexive || '';

        return letterboxdPronoun;
    }
}
