'use strict';

const { MessageEmbed } = require('discord.js');

class MessageEmbedFactory {
    createFollowSuccessMessage(data) {
        return this.createEmbed()
            .setDescription(
                `I am now following ${data.displayName} (${data.userName}) in this channel.\nI'll try to post their most recent entry.`,
            )
            .setThumbnail(data.image);
    }

    createDuplicateFollowMessage(data) {
        return this.createEmbed()
            .setDescription(
                `I was previously following ${data.displayName} (${data.userName}) in this channel.\nWe are already BFFs.`,
            )
            .setThumbnail(data.image);
    }

    createNoAccountFoundMessage(userName) {
        return this.createEmbed().setDescription(`I can't find **${userName}** on Letterboxd.`);
    }

    createUnfollowedSuccessMessage(data) {
        return this.createEmbed()
            .setDescription(
                `I unfollowed ${data.displayName} (${data.userName}) in this channel.\nNo hard feelings I hope.`,
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
        return this.createEmbed().setDescription('Not following any accounts in this channel');
    }

    createDiaryEntryMessage(entry, data, permissions) {
        const profileName = data.displayName;
        const profileURL = `https://letterboxd.com/${data.userName}/`;
        const profileImage = data.image;

        let releaseYear = entry.filmYear ? '(' + entry.filmYear + ')' : '';
        let dateString = '';
        if (entry.watchedDate) {
            const date = new Date(entry.watchedDate);
            const options = { month: 'short', day: 'numeric' };
            dateString = date.toLocaleDateString('default', options);
        }

        let reviewTitle = dateString + ' ';
        if (permissions?.use_external_emojis) {
            if (entry.starCount) {
                // Whole stars
                reviewTitle += '<:filmlinkd_star:851134022251970610>'.repeat(
                    Math.floor(entry.starCount),
                );
                // Half star if neccessary
                reviewTitle +=
                    entry.starCount % 1 ? '<:filmlinkd_half_star:851199023854649374>' : '';
            }
            if (entry.rewatch) {
                reviewTitle += ' <:filmlinkd_rewatch:851135667546488903>';
            }
            if (entry.liked) {
                reviewTitle += ' <:filmlinkd_like:851138401557676073>';
            }
        } else {
            reviewTitle =
                'Update Bot Permissions to Allow External Emojis: ' +
                'https://discord.com/oauth2/authorize?client_id=794271558570213409&permissions=262144&scope=bot';
        }

        let reviewText = entry.review || '<No Review>';
        if (reviewText.length > 400) {
            reviewText = reviewText.substring(0, 400).trim() + '…';
        }

        reviewText = entry.containsSpoilers
            ? '||`' + reviewText + '`||'
            : '```\n' + reviewText + '\n```';

        const embed = this.createEmbed()
            .setAuthor('Recent diary activity from ' + profileName, profileImage, profileURL)
            .setTitle(entry.filmTitle + ' ' + releaseYear)
            .setURL(entry.link)
            .setThumbnail(entry.image)
            .addField(reviewTitle, reviewText);

        // If there is footer data then include it.
        if (data.footer) {
            embed.setFooter(data.footer.text, data.footer.icon);
        }

        return embed;
    }

    createHelpMessage() {
        return this.createEmbed()
            .setTitle('(Help!) I Need Somebody')
            .setURL('https://jimlind.github.io/filmlinkd/')
            .addField('!help', 'View this help message.\n`!help`')
            .addField('!faq', 'Display a list of frequently asked questions.\n`!faq`')
            .addField(
                '!follow',
                'Subscribe Letterboxd user(s) in this channel.\n`!follow slim protolexus r0gue`',
            )
            .addField(
                '!unfollow',
                'Unsubscribe Letterboxd user(s) in this channel.\n`!unfollow slim protolexus r0gue`',
            )
            .addField(
                '!refresh',
                'Force refresh of Letterboxd user(s) display name and profile photo.\n`!refresh slim protolexus r0gue`',
            )
            .addField('!following', 'List all users subscribed in this channel.\n`!following`')
            .addField(
                ':clap: Patreon',
                '[Support the project on Patreon](https://www.patreon.com/filmlinkd)',
                true,
            )
            .addField(
                ':left_speech_bubble: Discord',
                '[Chat with a human on Discord](https://discord.gg/B4bk8h8PbZ)',
                true,
            );
    }

    createInadequatePermissionsMessage() {
        return this.createEmbed().setDescription(
            'Only users with Manage Server permissions are allowed to do that.',
        );
    }

    createEmbed() {
        return new MessageEmbed().setColor(0xa700bd);
    }
}

module.exports = MessageEmbedFactory;
