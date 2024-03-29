import LetterboxdPronoun from '../../models/letterboxd/letterboxd-pronoun.mjs';

export default class LetterboxdPronounFactory {
    /**
     * @param {Object} pronounData
     * @returns LetterboxdPronoun
     */
    buildPronounFromObject(pronounData) {
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
