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
            // Upper vs lower case vs numeric strings
            { args: ['abc', 'Abc'], expected: 1 }, // a is before A
            { args: ['Abc', 'abc'], expected: -1 }, // A is after a
            { args: ['1bc', 'abc'], expected: 1 }, // 1 is before a
            { args: ['abc', '1bc'], expected: -1 }, // a is after 1
            { args: ['1bc', 'Abc'], expected: 1 }, // 1 is before A
            { args: ['Abc', '1bc'], expected: -1 }, // A is after 1
            // Actual data
            { args: ['3hsy3B', '3m74fl'], expected: 1 }, // 3h is before 3m
            { args: ['3m74fl', '3hsy3B'], expected: -1 }, // 3m is after 3h
            { args: ['2bA6J9', '2Dzf87'], expected: 1 }, // 2b is before 2D
            { args: ['2bA6J9', '2456wT'], expected: -1 }, // 2b is after 24
            { args: ['2456wT', '2Dzf87'], expected: 1 }, // 24 is before 3D
        ];

        tests.forEach(({ args, expected }) => {
            const result = fixture.compare(args[0], args[1]);
            assert.equal(result, expected, 'Massive Failure');
        });
    });
});
