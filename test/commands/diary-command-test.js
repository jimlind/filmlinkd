const assert = require('assert');
const crypto = require('crypto');
const describe = require('mocha').describe;
const sinon = require('sinon');

const MessageEmbed = require('discord.js').MessageEmbed;

const DiaryCommand = require('../../commands/diary-command');
const LetterboxdLidWeb = require('../../services/letterboxd/letterboxd-lid-web');
const LetterboxdMemberApi = require('../../services/letterboxd/api/letterboxd-member-api');
const LetterboxdEntryApi = require('../../services/letterboxd/api/letterboxd-entry-api');
const MessageEmbedFactory = require('../../factories/message-embed-factory');

describe('Diary Command', () => {
    it('Account name passes directly into LID web scraper', async () => {
        const letterboxdLidWebStub = setupLetterboxdLidWeb();

        const diaryCommand = new DiaryCommand(
            letterboxdLidWebStub,
            setupLetterboxdMemberApi(),
            setupLetterboxdEntryApi(),
            setupMessageEmbedFactory(),
        );
        const accountName = getRandomString();
        await diaryCommand.getMessage(accountName);

        sinon.assert.calledOnceWithExactly(letterboxdLidWebStub.get, accountName);
    });

    it('Result of LID web scraper receives passed to API calls', async () => {
        const letterboxdLidWebStub = setupLetterboxdLidWeb();
        const lid = getRandomString();
        letterboxdLidWebStub.get.resolves(lid);

        const letterboxdMemberApiStub = setupLetterboxdMemberApi();
        const letterboxdEntryApiStub = setupLetterboxdEntryApi();

        const diaryCommand = new DiaryCommand(
            letterboxdLidWebStub,
            letterboxdMemberApiStub,
            letterboxdEntryApiStub,
            setupMessageEmbedFactory(),
        );
        await diaryCommand.getMessage();

        sinon.assert.calledOnceWithExactly(letterboxdMemberApiStub.getMember, lid);
        sinon.assert.calledOnceWithExactly(letterboxdEntryApiStub.get, lid, 5);
    });

    it('Result of member and entry API calls passed to message creation call', async () => {
        const letterboxdMemberApiStub = setupLetterboxdMemberApi();
        const member = sinon.stub;
        letterboxdMemberApiStub.getMember.resolves(member);

        const letterboxdEntryApiStub = setupLetterboxdEntryApi();
        const entryList = [sinon.stub];
        letterboxdEntryApiStub.get.resolves(entryList);

        const messageEmbedFactoryStub = setupMessageEmbedFactory();

        const diaryCommand = new DiaryCommand(
            setupLetterboxdLidWeb(),
            letterboxdMemberApiStub,
            letterboxdEntryApiStub,
            messageEmbedFactoryStub,
        );

        await diaryCommand.getMessage();

        sinon.assert.calledOnceWithExactly(
            messageEmbedFactoryStub.createDiaryListMessage,
            member,
            entryList,
        );
    });

    it('All successful promises means DiaryListMessage result returned', async () => {
        const messageEmbedFactoryStub = setupMessageEmbedFactory();
        const message = setupMessageEmbed();
        messageEmbedFactoryStub.createDiaryListMessage.returns(message);

        const diaryCommand = new DiaryCommand(
            setupLetterboxdLidWeb(),
            setupLetterboxdMemberApi(),
            setupLetterboxdEntryApi(),
            messageEmbedFactoryStub,
        );

        result = await diaryCommand.getMessage();

        sinon.assert.calledOnce(messageEmbedFactoryStub.createDiaryListMessage);
        sinon.assert.notCalled(messageEmbedFactoryStub.createNoAccountFoundMessage);

        assert.strictEqual(result, message);
    });

    it('If LID web scrape fails NoAccountFoundMessage result returned', async () => {
        const letterboxdLidWebStub = setupLetterboxdLidWeb();
        letterboxdLidWebStub.get.rejects();

        const messageEmbedFactoryStub = setupMessageEmbedFactory();
        const message = setupMessageEmbed();
        messageEmbedFactoryStub.createNoAccountFoundMessage.returns(message);

        const diaryCommand = new DiaryCommand(
            letterboxdLidWebStub,
            setupLetterboxdMemberApi(),
            setupLetterboxdEntryApi(),
            messageEmbedFactoryStub,
        );

        result = await diaryCommand.getMessage();

        sinon.assert.notCalled(messageEmbedFactoryStub.createDiaryListMessage);
        sinon.assert.calledOnce(messageEmbedFactoryStub.createNoAccountFoundMessage);

        assert.strictEqual(result, message);
    });

    it('If member API fails NoAccountFoundMessage result returned', async () => {
        const letterboxdMemberApiStub = setupLetterboxdMemberApi();
        letterboxdMemberApiStub.getMember.rejects();

        const messageEmbedFactoryStub = setupMessageEmbedFactory();
        const message = setupMessageEmbed();
        messageEmbedFactoryStub.createNoAccountFoundMessage.returns(message);

        const diaryCommand = new DiaryCommand(
            setupLetterboxdLidWeb(),
            letterboxdMemberApiStub,
            setupLetterboxdEntryApi(),
            messageEmbedFactoryStub,
        );

        result = await diaryCommand.getMessage();

        sinon.assert.notCalled(messageEmbedFactoryStub.createDiaryListMessage);
        sinon.assert.calledOnce(messageEmbedFactoryStub.createNoAccountFoundMessage);

        assert.strictEqual(result, message);
    });

    it('If entry API fails NoAccountFoundMessage result returned', async () => {
        const letterboxdEntryApiStub = setupLetterboxdEntryApi();
        letterboxdEntryApiStub.get.rejects();

        const messageEmbedFactoryStub = setupMessageEmbedFactory();
        const message = setupMessageEmbed();
        messageEmbedFactoryStub.createNoAccountFoundMessage.returns(message);

        const diaryCommand = new DiaryCommand(
            setupLetterboxdLidWeb(),
            setupLetterboxdMemberApi(),
            letterboxdEntryApiStub,
            messageEmbedFactoryStub,
        );

        result = await diaryCommand.getMessage();

        sinon.assert.notCalled(messageEmbedFactoryStub.createDiaryListMessage);
        sinon.assert.calledOnce(messageEmbedFactoryStub.createNoAccountFoundMessage);

        assert.strictEqual(result, message);
    });
});

/**
 * @returns {sinon.SinonStubbedInstance<LetterboxdLidWeb>}
 */
function setupLetterboxdLidWeb() {
    const letterboxdLidWeb = sinon.createStubInstance(LetterboxdLidWeb);
    letterboxdLidWeb.get.resolves();

    return letterboxdLidWeb;
}

/**
 * @returns {sinon.SinonStubbedInstance<LetterboxdMemberApi>}
 */
function setupLetterboxdMemberApi() {
    const letterboxdMemberApi = sinon.createStubInstance(LetterboxdMemberApi);
    letterboxdMemberApi.getMember.resolves();

    return letterboxdMemberApi;
}

/**
 * @returns {sinon.SinonStubbedInstance<LetterboxdEntryApi>}
 */
function setupLetterboxdEntryApi() {
    const letterboxdEntryApi = sinon.createStubInstance(LetterboxdEntryApi);
    letterboxdEntryApi.get.resolves();

    return letterboxdEntryApi;
}

/**
 * @returns {sinon.SinonStubbedInstance<MessageEmbedFactory>}
 */
function setupMessageEmbedFactory() {
    return sinon.createStubInstance(MessageEmbedFactory);
}

/**
 * @returns {sinon.SinonStubbedInstance<MessageEmbed>}
 */
function setupMessageEmbed() {
    return sinon.createStubInstance(MessageEmbed);
}

/**
 * @returns string
 */
function getRandomString() {
    return crypto.randomBytes(16).toString('hex');
}
