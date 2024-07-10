export default class TestCommand {
    /**
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     */
    constructor(embedBuilderFactory) {
        this.embedBuilderFactory = embedBuilderFactory;
    }

    /**
     * @returns {import('discord.js').EmbedBuilder[]}
     */
    process() {
        const result = [];

        const introduction =
            "This is the first of a series of test messages. If you can see this you know that basic command edit embeds work.\nIf you don't see any of the following messages you need to update your permissions as documented.\nNext you should see a basic embed message.";
        result.push(this.embedBuilderFactory.createEmbedBuilder().setDescription(introduction));

        const basicEmbed =
            'This is a basic embed message.\nNext you should see an embed with a simple emoji.';
        result.push(this.embedBuilderFactory.createEmbedBuilder().setDescription(basicEmbed));

        const simpleEmojiEmbed =
            'This is a embed message with simple emoji :star::star::star:.\nNext you should see an embed with a custom emoji.';
        result.push(this.embedBuilderFactory.createEmbedBuilder().setDescription(simpleEmojiEmbed));

        const customEmojiEmbed =
            'This is a embed message with custom emoji <:s:851134022251970610><:s:851134022251970610><:s:851134022251970610>.\nNext you should see an embed with formatted text.';
        result.push(this.embedBuilderFactory.createEmbedBuilder().setDescription(customEmojiEmbed));

        const formattedEmbed =
            'This is a embed message *with* **formatted** ***text***.\nNext you should see an embed with an image.';
        result.push(this.embedBuilderFactory.createEmbedBuilder().setDescription(formattedEmbed));

        const imageEmbed =
            'This is a embed message with an image.\nThis concludes this test of the Emergency Broadcast System..';
        result.push(
            this.embedBuilderFactory
                .createEmbedBuilder()
                .setDescription(imageEmbed)
                .setThumbnail('https://jimlind.github.io/filmlinkd/images/filmlinkd-100.png'),
        );

        return result;
    }
}
