const LetterboxdMember = require('../models/letterboxd/letterboxd-member');

class LetterboxdMemberFactory {
    propertyMap = {
        id: 'id',
        userName: 'username',
        displayName: 'displayName',
        image: 'avatar//sizes//1//url',
        pronoun: 'pronoun//label',
    };

    /**
     * @param {Object} memberData
     * @returns LetterboxdMember
     */
    buildFromObject(memberData) {
        const letterboxdMember = new LetterboxdMember();
        for (const targetPropertyKey in letterboxdMember) {
            const sourcePropertyKey = this.propertyMap[targetPropertyKey];
            if (!sourcePropertyKey) {
                continue;
            }

            let sourceData = memberData;
            const keyList = sourcePropertyKey.split('//');
            while (keyList.length) {
                const key = keyList.shift();
                if (keyList.length) {
                    sourceData = sourceData[key] || {};
                } else {
                    letterboxdMember[targetPropertyKey] = sourceData[key] || '';
                }
            }
        }

        return letterboxdMember;
    }
}

module.exports = LetterboxdMemberFactory;
