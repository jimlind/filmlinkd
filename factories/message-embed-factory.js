'use strict';

const { MessageEmbed } = require('discord.js');

class MessageEmbedFactory {
    /**
     * @param {import('markdown-truncate')} truncateMarkdown
     * @param {import('turndown')} turndownService
     */
    constructor(truncateMarkdown, turndownService) {
        this.truncateMarkdown = truncateMarkdown;
        this.turndownService = turndownService;
    }

    createFollowSuccessMessage(data) {
        return this.createEmbed()
            .setDescription(
                `I am now following ${data.displayName} (${data.userName}).\nI'll try to post their most recent entry in the appropriate channel.`,
            )
            .setThumbnail(data.image);
    }

    createDuplicateFollowMessage(data) {
        return this.createEmbed()
            .setDescription(
                `I was previously following ${data.displayName} (${data.userName}).\nWe are already BFFs.`,
            )
            .setThumbnail(data.image);
    }

    createNoAccountFoundMessage(userName) {
        return this.createEmbed().setDescription(`I can't find **${userName}** on Letterboxd.`);
    }

    createUnfollowedSuccessMessage(data) {
        return this.createEmbed()
            .setDescription(
                `I unfollowed ${data.displayName} (${data.userName}).\nNo hard feelings I hope.`,
            )
            .setThumbnail(data.image);
    }

    createUnfollowedErrorMessage(userName) {
        return this.createEmbed().setDescription(`Unable to unfollow ${userName}.`);
    }

    createRefreshSuccessMessage(data) {
        return this.createEmbed()
            .setDescription(`I updated my display data for ${data.displayName} (${data.userName}).`)
            .setThumbnail(data.image);
    }

    createRefreshErrorMessage(userName) {
        return this.createEmbed().setDescription(`Unable to refresh ${userName}.`);
    }

    createFollowingMessage(results) {
        const accountNameString = results.join(' ');
        return this.createEmbed().setDescription(
            "Here are the accounts I'm following:" + '```\n' + accountNameString + '\n```',
        );
    }

    createEmptyFollowingMessage() {
        return this.createEmbed().setDescription('Not following any accounts.');
    }

    /**
     * @param {import("../models/diary-entry")} entry
     * @param {import("../models/user")} data
     */
    createDiaryEntryMessage(entry, data) {
        const profileName = data.displayName;
        const profileURL = `https://letterboxd.com/${data.userName}/`;
        const profileImage = data.image;

        const action = entry.type || 'logg';
        const authorTitle = `${profileName} ${action}ed...`;

        const adult = entry.adult ? ':underage: ' : '';
        const releaseYear = entry.filmYear ? '(' + entry.filmYear + ')' : '';
        let dateString = '';
        if (entry.watchedDate) {
            const date = new Date(entry.watchedDate);
            dateString = date.toLocaleDateString('default', { month: 'short', day: 'numeric' });
        }

        let reviewTitle = dateString ? '**' + dateString + '** ' : '';
        if (entry.starCount) {
            // Whole stars
            const rounded = Math.floor(entry.starCount);
            reviewTitle += '<:s:851134022251970610>'.repeat(rounded);
            // Half star if neccessary
            reviewTitle += entry.starCount % 1 ? '<:h:851199023854649374>' : '';
        }
        if (entry.rewatch) {
            reviewTitle += ' <:r:851135667546488903>';
        }
        if (entry.liked) {
            reviewTitle += ' <:l:851138401557676073>';
        }
        reviewTitle = reviewTitle ? reviewTitle + '\u200b\n' : '';

        let reviewText = this.turndownService.turndown(entry.review);
        reviewText = this.truncateMarkdown(reviewText, { limit: 400, ellipsis: true });
        reviewText = entry.containsSpoilers ? '||' + reviewText + '||' : reviewText;

        const rule = reviewTitle && reviewText ? '┈'.repeat(12) + '\n' : '';
        const embed = this.createEmbed()
            .setAuthor(authorTitle, profileImage, profileURL)
            .setTitle(adult + entry.filmTitle + ' ' + releaseYear)
            .setURL(entry.link)
            .setThumbnail(entry.image)
            .setDescription(reviewTitle + rule + reviewText);

        // If there is footer data with actual data then include it.
        if (data.footer?.text || data.footer?.icon) {
            embed.setFooter(data.footer.text, data.footer.icon);
        }

        return embed;
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-member)} member
     * @param {import('../models/letterboxd/letterboxd-entry')[]} entryList
     * @returns {MessageEmbed}
     */
    createDiaryListMessage(member, entryList) {
        const entryTextList = entryList.map((entry) => {
            let entryFirstLine = `[**${entry.filmName} (${entry.filmYear})**](https://boxd.it/${entry.id})`;
            let entrySecondLine = entry.date ? `${this.formatDate(entry.date)}` : '';
            entrySecondLine += this.formatStars(entry.rating);
            entrySecondLine += entry.rewatch ? ' <:r:851135667546488903>' : '';
            entrySecondLine += entry.like ? ' <:r:851138401557676073>' : '';
            entrySecondLine += entry.review ? ' :speech_balloon:' : '';

            return entryFirstLine + '\n' + entrySecondLine;
        }, '');

        return this.createEmbed()
            .setTitle(`Recent Diary Activity from ${member.displayName}`)
            .setURL(`https://boxd.it/${member.id}`)
            .setThumbnail(member.image)
            .setDescription(entryTextList.join('\n'));
    }

    createHelpMessage() {
        return this.createEmbed()
            .setTitle('(Help!) I Need Somebody')
            .setURL('https://jimlind.github.io/filmlinkd/')
            .addField('/help', 'Replies with a some helpful information and links.')
            .addField(
                '/follow <account>',
                'Adds the Letterboxd account to the following list this channel.',
            )
            .addField(
                '/unfollow <account>',
                'Removes the Letterboxd account from the following list in this channel.',
            )
            .addField(
                '/refresh <account>',
                'Updates the Filmlinkd cache for the Letterboxd account.',
            )
            .addField('/following', 'Replies with a list of all accounts followed in this channel.')
            .addField(
                ':clap: Patreon',
                '[Support the project on Patreon](https://www.patreon.com/filmlinkd)',
                true,
            )
            .addField(
                ':left_speech_bubble: Discord',
                '[Chat with a human on Discord](https://discord.gg/deZ7EUguge)',
                true,
            );
    }

    createInadequatePermissionsMessage() {
        return this.createEmbed().setDescription(
            'Only users with Manage Server permissions are allowed to do that.',
        );
    }

    createChannelNotFoundMessage() {
        return this.createEmbed().setDescription('Unable to find the specified channel.');
    }

    createEmbed() {
        return new MessageEmbed().setColor(0xa700bd);
    }

    formatStars(rating) {
        if (!rating) {
            return '';
        }

        // Whole stars
        const rounded = Math.floor(rating);
        let starString = '<:s:851134022251970610>'.repeat(rounded);
        // Half star if neccessary
        starString += rating % 1 ? '<:h:851199023854649374>' : '';

        return starString;
    }

    /**
     * @param {Date} date
     * @returns string
     */
    formatDate(date) {
        const recentFormat = { month: 'short', day: 'numeric' };
        const pastFormat = { month: 'short', day: 'numeric', year: 'numeric' };
        //const format = date - new Date() > 5000000000 ? recentFormat : pastFormat;
        const format = new Date() - date < 5000000000 ? recentFormat : pastFormat;

        console.log({
            d: new Date() - date,
            f: new Date() - date > 5000000000,
        });

        return date.toLocaleDateString('default', format) + ' ';
    }
}

module.exports = MessageEmbedFactory;
