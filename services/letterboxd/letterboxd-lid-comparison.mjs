export default class LetterboxdLidComparison {
    /**
     * Compare Letterboxd IDs
     * 1: A is before B
     * 0: A is the same as B
     * -1 A is after B
     *
     * Strings are normally compared lowest to highest [0-9][A-Z][a-z]
     * Letterboxd LIDs are compared lowest to highest [0-9][a-z][A-Z]
     * So we need to swap cases before doing a comparison
     *
     * @param {string} letterboxdIdA
     * @param {string} letterboxdIdB
     * @returns {number}
     */
    compare(letterboxdIdA, letterboxdIdB) {
        const low = (x) => x.toLowerCase();
        const up = (x) => x.toUpperCase();
        const swapCase = (x) => [...x].map((c) => (c === low(c) ? up(c) : low(c))).join('');

        if (letterboxdIdA === letterboxdIdB) {
            return 0;
        }

        if (letterboxdIdA.length === letterboxdIdB.length) {
            return swapCase(letterboxdIdA) < swapCase(letterboxdIdB) ? 1 : -1;
        } else {
            return letterboxdIdA.length < letterboxdIdB.length ? 1 : -1;
        }
    }
}
