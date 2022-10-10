const describe = require('mocha').describe;
const assert = require('assert');

const LetterboxdLidComparison = require('../../../services/letterboxd/letterboxd-lid-comparison');

describe('Letterboxd ID Comparison', () => {
    it('should logically compare the letterboxd ids', () => {
        const fixture = new LetterboxdLidComparison();

        const tests = [
            // The same strings
            { args: ['abc', 'abc'], expected: 0 },
            { args: ['123', '123'], expected: 0 },
            { args: ['ab12', 'ab12'], expected: 0 },
            { args: ['12ab', '12ab'], expected: 0 },
            // Different length strings
            { args: ['abcd', 'abc'], expected: -1 },
            { args: ['abc', 'abcd'], expected: 1 },
            // Upper vs lower case strings
            { args: ['Abc', 'abc'], expected: 1 },
            { args: ['abc', 'Abc'], expected: -1 },
        ];

        tests.forEach(({ args, expected }) => {
            const result = fixture.compare(args[0], args[1]);
            assert.equal(result, expected, 'Massive Failure');
        });
    });
});
