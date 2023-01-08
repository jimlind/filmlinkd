const assert = require('assert');
const crypto = require('crypto');
const describe = require('mocha').describe;
const sinon = require('sinon');
const MessageEmbed = require('discord.js').MessageEmbed;

const ContributorCommand = require('../../commands/contributor-command');
const LetterboxdContributorApi = require('../../services/letterboxd/api/letterboxd-contributor-api');
const MessageEmbedFactory = require('../../factories/message-embed-factory');

describe('Contributor Command', () => {
    it('Input passes directly into the API', () => {
        const apiStub = setupLetterboxdContributorApi();
        apiStub.getContributor.resolves();

        const contributorCommand = new ContributorCommand(apiStub, setupMessageEmbedFactory());
        const contributorInput = getRandomString();
        contributorCommand.getMessage(contributorInput);

        sinon.assert.calledOnceWithExactly(apiStub.getContributor, contributorInput);
    });

    it('Successful promise calls and returns contributor message method', async () => {
        const apiStub = setupLetterboxdContributorApi();
        const contributorOutput = getRandomString();
        apiStub.getContributor.resolves(contributorOutput);

        const messageFactoryStub = setupMessageEmbedFactory();
        const message = setupMessageEmbed();
        messageFactoryStub.createContributorMessage.returns(message);

        const contributorCommand = new ContributorCommand(apiStub, messageFactoryStub);
        result = await contributorCommand.getMessage();

        sinon.assert.calledOnceWithExactly(
            messageFactoryStub.createContributorMessage,
            contributorOutput,
        );
        sinon.assert.notCalled(messageFactoryStub.createContributorNotFoundMessage);
        assert.strictEqual(result, message);
    });

    it('Unsuccessful promise calls and returns not found message method', async () => {
        const apiStub = setupLetterboxdContributorApi();
        apiStub.getContributor.rejects();

        const messageFactoryStub = setupMessageEmbedFactory();
        const message = sinon.createStubInstance(MessageEmbed);
        messageFactoryStub.createContributorNotFoundMessage.returns(message);

        const contributorCommand = new ContributorCommand(apiStub, messageFactoryStub);
        result = await contributorCommand.getMessage();

        sinon.assert.notCalled(messageFactoryStub.createContributorMessage);
        sinon.assert.calledOnce(messageFactoryStub.createContributorNotFoundMessage);
        assert.strictEqual(result, message);
    });
});

/**
 * @returns {sinon.SinonStubbedInstance<LetterboxdContributorApi>}
 */
function setupLetterboxdContributorApi() {
    return sinon.createStubInstance(LetterboxdContributorApi);
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
