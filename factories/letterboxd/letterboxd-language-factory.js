const LetterboxdLanguage = require('../../models/letterboxd/letterboxd-language');

class LetterboxdLanguageFactory {
    /**
     * @param {Object} languageData
     * @returns LetterboxdLanguage
     */
    buildLanguageFromObject(languageData) {
        const letterboxdLanguage = new LetterboxdLanguage();

        letterboxdLanguage.code = languageData.code;
        letterboxdLanguage.name = languageData.name;

        return letterboxdLanguage;
    }
}

module.exports = LetterboxdLanguageFactory;
