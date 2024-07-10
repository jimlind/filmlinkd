import LetterboxdLanguage from '../../models/letterboxd/letterboxd-language.js';

export default class LetterboxdLanguageFactory {
    /**
     * @param {Object} languageData
     * @returns LetterboxdLanguage
     */
    buildLanguageFromObject(languageData: any) {
        const letterboxdLanguage = new LetterboxdLanguage();

        letterboxdLanguage.code = languageData.code;
        letterboxdLanguage.name = languageData.name;

        return letterboxdLanguage;
    }
}
