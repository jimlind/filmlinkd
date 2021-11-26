const LetterboxdMember = require('../models/letterboxd/letterboxd-member');

class LetterboxdMemberFactory {
    propertyMap = {
        id: 'id',
        displayName: 'displayName',
        pronoun: 'pronoun//label',
    };

    /**
     * @param {Object} memberData
     * @returns LetterboxdMember
     */
    buildFromObject(memberData) {
        const letterboxdMember = new LetterboxdMember();
        letterboxdMember.userName = memberData.username.toLowerCase();

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

        const largestImage = memberData.avatar.sizes.reduce((previous, current) =>
            current.height > previous.height ? current : previous,
        );
        letterboxdMember.image = largestImage.url;

        return letterboxdMember;
    }
}

module.exports = LetterboxdMemberFactory;
