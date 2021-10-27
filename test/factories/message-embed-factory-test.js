const assert = require('assert');
const describe = require('mocha').describe;

const crypto = require('crypto');
const DiaryEntry = require('../../models/diary-entry');
const MessageEmbedFactory = require('../../factories/message-embed-factory');
const User = require('../../models/user');

describe('Message Embed Factory', () => {
    it('review normal descriptions are properly formatted', () => {
        const randomText = crypto.randomBytes(16).toString('hex');

        const entry = new DiaryEntry();
        entry.review = randomText;
        const user = new User();

        const messageEmbedFactory = new MessageEmbedFactory();
        const message = messageEmbedFactory.createDiaryEntryMessage(entry, user);

        assert.strictEqual(message.description, randomText);
    });

    it('spoiler reviews are properly formatted', () => {
        const randomText = crypto.randomBytes(16).toString('hex');

        const user = new User();
        const entry = new DiaryEntry();
        entry.containsSpoilers = true;
        entry.review = randomText;

        const messageEmbedFactory = new MessageEmbedFactory();
        const message = messageEmbedFactory.createDiaryEntryMessage(entry, user);

        const formattedText = '||' + randomText + '||';
        assert.strictEqual(message.description, formattedText);
    });
});
