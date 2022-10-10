'use strict';

class LetterboxdLidComparison {
    /**
     * Compare Letterboxd IDs
     * 1: A is before B
     * 0: A is the same as B
     * -1 A is after B
     *
     * @param {string} letterboxdIdA
     * @param {string} letterboxdIdB
     * @returns {number}
     */
    compare(letterboxdIdA, letterboxdIdB) {
        if (letterboxdIdA === letterboxdIdB) {
            return 0;
        }

        if (letterboxdIdA.length === letterboxdIdB.length) {
            return letterboxdIdA < letterboxdIdB ? 1 : -1;
        } else {
            return letterboxdIdA.length < letterboxdIdB.length ? 1 : -1;
        }
    }
}

module.exports = LetterboxdLidComparison;
