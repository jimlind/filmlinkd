const LetterboxdCountry = require('../../models/letterboxd/letterboxd-country');

class LetterboxdCountryFactory {
    /**
     * @param {Object} countryData
     * @returns LetterboxdCountry
     */
    buildCountryFromObject(countryData) {
        const letterboxdCountry = new LetterboxdCountry();

        letterboxdCountry.code = countryData.code;
        letterboxdCountry.name = countryData.name;

        return letterboxdCountry;
    }
}

module.exports = LetterboxdCountryFactory;
