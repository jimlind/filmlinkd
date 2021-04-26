const assert = require('assert');
const describe = require('mocha').describe;

const crypto = require('crypto');
const DiaryEntry = require('../../models/diary-entry');
const MessageEmbedFactory = require('../../factories/message-embed-factory');

describe('Message Embed Factory', () => {
    it('review codeblocks are properly formatted', () => {
        const randomText = crypto.randomBytes(16).toString('hex');

        const data = [];
        const entry = new DiaryEntry();
        entry.review = randomText;

        const messageEmbedFactory = new MessageEmbedFactory();
        const message = messageEmbedFactory.createDiaryEntryMessage(entry, data);

        const formattedText = '```\n' + randomText + '\n```';
        assert.strictEqual(message.fields[0].value, formattedText);
    });

    it('spoiler reviews are properly formatted', () => {
        const randomText = crypto.randomBytes(16).toString('hex');

        const data = [];
        const entry = new DiaryEntry();
        entry.containsSpoilers = true;
        entry.review = randomText;

        const messageEmbedFactory = new MessageEmbedFactory();
        const message = messageEmbedFactory.createDiaryEntryMessage(entry, data);

        const formattedText = '||`' + randomText + '`||';
        assert.strictEqual(message.fields[0].value, formattedText);
    });
});
