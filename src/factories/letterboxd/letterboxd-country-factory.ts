import LetterboxdCountry from '../../models/letterboxd/letterboxd-country.js';

export default class LetterboxdCountryFactory {
    /**
     * @param {Object} countryData
     * @returns LetterboxdCountry
     */
    buildCountryFromObject(countryData: any) {
        const letterboxdCountry = new LetterboxdCountry();

        letterboxdCountry.code = countryData.code;
        letterboxdCountry.name = countryData.name;

        return letterboxdCountry;
    }
}
