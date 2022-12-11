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

    createFollowingMessage(resultList) {
        const resultListSorted = resultList.sort((a, b) => {
            let lidA = a.lid;
            let lidB = b.lid;
            if (lidA.length != lidB.length) {
                const newLength = Math.max(lidA.length, lidB.length);
                lidA = lidA.padStart(newLength, '#');
                lidB = lidB.padStart(newLength, '#');
            }
            return lidA > lidB ? 1 : -1;
        });

        const resultTextList = resultListSorted.reduce((previous, current) => {
            const string = `• [${current.userName} (${current.lid})](https://boxd.it/${current.lid})\n`;
            return previous + string;
        }, '');

        return this.createEmbed().setDescription(
            "Here are the accounts I'm following:" + '\n' + resultTextList,
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
     * @param {import('../models/letterboxd/letterboxd-member')} member
     * @param {import('../models/letterboxd/letterboxd-entry')[]} entryList
     * @returns {MessageEmbed}
     */
    createDiaryListMessage(member, entryList) {
        const entryTextList = entryList.map((entry) => {
            let entryFirstLine = `[**${entry.filmName} (${entry.filmYear})**](https://boxd.it/${entry.id})`;
            let entrySecondLine = entry.date ? `${this.formatDate(entry.date)} ` : '';
            entrySecondLine += this.formatStars(entry.rating);
            entrySecondLine += entry.rewatch ? ' <:r:851135667546488903>' : '';
            entrySecondLine += entry.like ? ' <:l:851138401557676073>' : '';
            entrySecondLine += entry.review ? ' :speech_balloon:' : '';

            return entryFirstLine + '\n' + entrySecondLine;
        }, '');

        const largestImage = member.avatar.sizes.reduce((previous, current) =>
            current.height > previous.height ? current : previous,
        );

        return this.createEmbed()
            .setTitle(`Recent Diary Activity from ${member.displayName}`)
            .setURL(`https://boxd.it/${member.id}`)
            .setThumbnail(largestImage.url)
            .setDescription(entryTextList.join('\n'));
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-film')} film
     * @param {import('../models/letterboxd/letterboxd-film-statistics')} filmStatistics
     * @returns {MessageEmbed}
     */
    createFilmMessage(film, filmStatistics) {
        let description = '';

        // Add tagline to description
        if (film.tagline) {
            description += `**${film.tagline}**\n`;
        }

        // Add star emoji and text to description
        if (filmStatistics.rating) {
            const starEmoji = this.formatStars(filmStatistics.rating);
            description += `${starEmoji} ${filmStatistics.rating.toFixed(2)} \n`;
        }

        // Add directors to description
        const filteredDirectorList = film.contributions.filter((c) => c.type == 'Director');
        const directorLinkList = (filteredDirectorList[0]?.contributors || []).map(
            (contributor) => `[${contributor.name}](https://boxd.it/${contributor.id})`,
        );
        if (directorLinkList.length) {
            description += `Director(s): ${directorLinkList.join(', ')}\n`;
        }

        // Add runtime and countries to description
        const countryList = film.countries.map((country) => country?.name || '').filter(Boolean);
        const runTimeString = this.formatRuntime(film.runTime);
        const lineStrings = [countryList.join(', '), runTimeString].filter(Boolean);
        if (lineStrings.length) {
            description += `${lineStrings.join(' | ')}\n`;
        }

        // Add genre to description
        const genreList = film.genres.map((genre) => genre?.name || '').filter(Boolean);
        if (genreList.length) {
            description += genreList.join('/') + '\n';
        }

        // Add additional statistics emoji and numbers
        description +=
            `:eyes: ${this.formatCount(filmStatistics.watchCount)}, ` +
            `<:r:851138401557676073> ${this.formatCount(filmStatistics.likeCount)}, ` +
            `:speech_balloon: ${this.formatCount(filmStatistics.reviewCount)}\n`;

        const largestImage = (film?.poster?.sizes || []).reduce(
            (previous, current) => (current.height || 0 > previous.height ? current : previous),
            {},
        );

        return this.createEmbed()
            .setTitle(`${film.name} (${film.releaseYear})`)
            .setURL(`https://boxd.it/${film.id}`)
            .setThumbnail(largestImage?.url || '')
            .setDescription(description);
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-member')} member
     * @param {import('../models/letterboxd/letterboxd-member-statistics')} memberStatistics
     * @returns {MessageEmbed}
     */
    createUserMessage(member, memberStatistics) {
        let description = '';

        // Add member location to description
        if (member.location) {
            description += `***${member.location}***\n`;
        }
        // Add member bio and horizontal rule to description
        if (member.bioLbml) {
            description +=
                this.truncateMarkdown(this.turndownService.turndown(member.bio), {
                    limit: 1000,
                    ellipsis: true,
                }) + '\n';
            description += '┈'.repeat(12) + '\n';
        }

        // Add favorite film list to description
        const filmList = member.favoriteFilms.map(
            (film) => `- [${film.name} (${film.releaseYear})](https://boxd.it/${film.id})`,
        );
        description += filmList.join('\n') + '\n';

        // Add member film counts to description
        const counts = memberStatistics.counts;
        description += `Logged films: ${counts.watches} total | ${counts.filmsInDiaryThisYear} this year`;

        const pronounList = [
            member.pronoun.subjectPronoun,
            member.pronoun.objectPronoun,
            member.pronoun.possessivePronoun,
        ];

        return this.createEmbed()
            .setTitle(`${member.displayName} (${pronounList.join('/')})`)
            .setURL(`https://boxd.it/${member.id}`)
            .setThumbnail(this.parseImage(member?.avatar?.sizes))
            .setDescription(description);
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-list-summary')} listSummary
     * @returns {MessageEmbed}
     */
    createListMessage(listSummary) {
        let description = `**List of ${listSummary.filmCount} films curated by [${listSummary.owner.displayName}](https://boxd.it/${listSummary.owner.id})**\n`;
        description += this.turndownService.turndown(listSummary.description) + '\n';

        // Add film list to description
        const filmList = listSummary.previewEntries
            .map(
                (entry) =>
                    `${entry.rank || '-'} [${entry.film.name} (${
                        entry.film.releaseYear
                    })](https://boxd.it/${entry.film.id})`,
            )
            .slice(0, 6);
        description += filmList.join('\n');
        if (listSummary.filmCount > 6) {
            description += ' ...';
        }

        return this.createEmbed()
            .setTitle(listSummary.name)
            .setURL(`https://boxd.it/${listSummary.id}`)
            .setThumbnail(this.parseImage(listSummary?.previewEntries[0]?.film?.poster?.sizes))
            .setDescription(description);
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-log-entry')[]} logEntryList
     * @returns {MessageEmbed}
     */
    createLoggedMessage(logEntryList) {
        if (!logEntryList.length) {
            throw 'Empty List of Log Entry Provided';
        }

        const logEntryTextList = logEntryList.map((logEntry) => {
            const actionString = logEntry.review ? 'Reviewed' : 'Watched';
            const dateString = this.formatDate(new Date(logEntry?.diaryDetails?.diaryDate));
            const activityLine = `[**${actionString} on ${dateString}**](https://boxd.it/${logEntry.id})`;

            let emojiLine = this.formatStars(Number(logEntry?.rating));
            emojiLine += logEntry?.diaryDetails?.rewatch ? ' <:r:851135667546488903>' : '';
            emojiLine += logEntry?.like ? ' <:l:851138401557676073>' : '';

            let reviewText = this.turndownService.turndown(logEntry?.review?.text || '');
            reviewText = this.truncateMarkdown(reviewText, { limit: 200, ellipsis: true });
            reviewText = logEntry?.review?.containsSpoilers ? '||' + reviewText + '||' : reviewText;

            return [activityLine, emojiLine.trim(), reviewText].filter(Boolean).join('\n');
        });

        const firstEntry = logEntryList[0];
        const title = `${firstEntry?.owner?.displayName}'s Recent Entries for ${firstEntry?.film?.name} (${firstEntry?.film?.releaseYear})`;

        return this.createEmbed()
            .setTitle(title)
            .setThumbnail(this.parseImage(logEntryList[0]?.film?.poster?.sizes))
            .setDescription(logEntryTextList.join('\n'));
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-contributor')} contributor
     * @returns {MessageEmbed}
     */
    createContributorMessage(contributor) {
        const filmographyString = contributor.statistics.contributions
            .reduce((acc, contribution) => {
                acc.push(`**${contribution.type}**: ${contribution.filmCount}`);
                return acc;
            }, [])
            .join('\n');

        const linkString = contributor.links
            .reduce((acc, link) => {
                acc.push(`[${link.type}](${link.url})`);
                return acc;
            }, [])
            .join(' | ');

        return this.createEmbed()
            .setTitle(contributor.name)
            .setURL(`https://boxd.it/${contributor.id}`)
            .setDescription(filmographyString + '\n' + linkString);
    }

    /**
     *
     * @param {import('../models/config')} config
     * @param {number} userCount
     * @param {number} serverCount
     * @returns
     */
    createHelpMessage(config, userCount, serverCount) {
        const description = `${config?.packageName} v${config?.packageVersion}\nTracking ${userCount} users on ${serverCount} servers`;

        return this.createEmbed()
            .setTitle('(Help!) I Need Somebody')
            .setURL('https://jimlind.github.io/filmlinkd/')
            .setDescription(description)
            .addField('/help', 'Shows this message')
            .addField('/follow account [channel]', 'Listens for new entries')
            .addField('/unfollow account [channel]', 'Stops listening for new entries')
            .addField('/following', 'List all users followed in this channel')
            .addField('/refresh account', 'Refreshes the Filmlinkd cache for the account')
            .addField('/contributor contributor-name', "Shows a film contributor's information")
            .addField('/diary account', "Shows a user's 5 most recent entries")
            .addField('/film film-name', "Shows a film's information")
            .addField('/list account list-name', "Shows a users's list's information")
            .addField('/logged account film-name', "Shows a user's entries for a film")
            .addField('/roulette', 'Shows random film information')
            .addField('/user account', "Shows a users's information")
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

    createChannelNotFoundMessage() {
        return this.createEmbed().setDescription('Unable to find the specified channel.');
    }

    createFilmNotFoundMessage() {
        return this.createEmbed().setDescription('Unable to match a film to those search terms.');
    }

    createContributorNotFoundMessage() {
        return this.createEmbed().setDescription(
            'Unable to match a contributor to those search terms.',
        );
    }

    createNoListFoundMessage() {
        const message = 'Unable to match an account list to those search terms.';
        return this.createEmbed().setDescription(message);
    }

    createNoLoggedEntriesFoundMessage() {
        const message = 'Unable to find that film logged for that user.';
        return this.createEmbed().setDescription(message);
    }

    createEmbed() {
        return new MessageEmbed().setColor(0xa700bd);
    }

    formatStars(rating) {
        if (!rating) {
            return '';
        }

        // Turn into displayable partial stars
        const cleanRating = Math.round(rating * 2) / 2;

        // Whole stars
        const rounded = Math.floor(cleanRating);
        let starString = '<:s:851134022251970610>'.repeat(rounded);
        // Half star if neccessary
        starString += cleanRating % 1 ? '<:h:851199023854649374>' : '';

        return starString;
    }

    /**
     * @param {Date} date
     * @returns string
     */
    formatDate(date) {
        if (!(date instanceof Date) || isNaN(date)) {
            return 'Unknown Date';
        }

        const recentFormat = { month: 'short', day: 'numeric' };
        const pastFormat = { month: 'short', day: 'numeric', year: 'numeric' };
        const format = new Date() - date < 5000000000 ? recentFormat : pastFormat;

        return date.toLocaleDateString('default', format);
    }

    /**
     * @param {number} runTime minutes
     * @return string
     */
    formatRuntime(runTime) {
        if (!runTime) {
            return '';
        }

        const hours = Math.floor(runTime / 60);
        const minutes = runTime - hours * 60;

        return `${hours}h ${minutes}m`;
    }

    /**
     * @param {number} count
     * @return string
     */
    formatCount(count) {
        const countString = count.toString();
        let suffix = '';
        if (countString.length >= 10) {
            suffix = 'b';
            count = count / 1000000000;
        } else if (countString.length >= 7) {
            suffix = 'm';
            count = count / 1000000;
        } else if (countString.length >= 4) {
            suffix = 'k';
            count = count / 1000;
        }

        let truncated = Math.round(count);
        if (truncated < 100 && suffix) {
            truncated = count.toFixed(1);
        }

        return truncated + suffix;
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-image-size')[]} sizes
     * return string
     */
    parseImage(sizes) {
        const findLargest = (previous, current) =>
            current.height || 0 > previous.height ? current : previous;
        const largestImage = (sizes || []).reduce(findLargest, {});
        return largestImage?.url || '';
    }
}

module.exports = MessageEmbedFactory;
