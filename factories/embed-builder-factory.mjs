export default class EmbedBuilderFactory {
    /**
     * @param {import('discord.js')} discordLibrary
     * @param {import('../services/letterboxd/letterboxd-lid-comparison.mjs')} letterboxdLidComparison
     * @param {import('markdown-truncate')} truncateMarkdown
     * @param {import('turndown')} turndownService
     */
    constructor(discordLibrary, letterboxdLidComparison, truncateMarkdown, turndownService) {
        this.discordLibrary = discordLibrary;
        this.letterboxdLidComparison = letterboxdLidComparison;
        this.truncateMarkdown = truncateMarkdown;
        this.turndownService = turndownService;
    }

    createFollowSuccessEmbed(data) {
        const description = this.formatDescription(
            `I am now following ${data.displayName} (${data.userName}).\nI'll try to post their most recent entry in the appropriate channel.`,
        );
        return this.createEmbedBuilder().setDescription(description).setThumbnail(data.image);
    }

    createDuplicateFollowEmbed(data) {
        const description = this.formatDescription(
            `I was previously following ${data.displayName} (${data.userName}).\nWe are already BFFs.`,
        );
        return this.createEmbedBuilder().setDescription(description).setThumbnail(data.image);
    }

    createNoAccountFoundEmbed(userName) {
        const description = this.formatDescription(`I can't find **${userName}** on Letterboxd.`);
        return this.createEmbedBuilder().setDescription(description);
    }

    createUnfollowedSuccessEmbed(data) {
        const description = this.formatDescription(
            `I unfollowed ${data.displayName} (${data.userName}).\nNo hard feelings I hope.`,
        );
        return this.createEmbedBuilder().setDescription(description).setThumbnail(data.image);
    }

    createUnfollowedErrorEmbed(userName) {
        const description = this.formatDescription(`Unable to unfollow ${userName}.`);
        return this.createEmbedBuilder().setDescription(description);
    }

    createRefreshSuccessEmbed(data) {
        const description = this.formatDescription(
            `I updated my display data for ${data.displayName} (${data.userName}).`,
        );
        return this.createEmbedBuilder().setDescription(description).setThumbnail(data.image);
    }

    createRefreshErrorEmbed(userName) {
        const description = this.formatDescription(`Unable to refresh ${userName}.`);
        return this.createEmbedBuilder().setDescription(description);
    }

    /**
     * @param {*[]} userList
     * @returns {EmbedBuilder[]}
     */
    createFollowingEmbedList(userList) {
        const result = [];

        // Create initial message
        const description = this.formatDescription("Here are the accounts I'm following...");
        result.push(this.createEmbedBuilder().setDescription(description));

        // Sort list of followers by User LID
        const lidSorter = (a, b) => -1 * this.letterboxdLidComparison.compare(a.lid, b.lid);
        const sortedUserList = userList.sort(lidSorter);

        let resultString = '';
        for (let x = 0; x < sortedUserList.length; x++) {
            const user = sortedUserList[x];
            const nextString =
                resultString + `• [${user.userName} (${user.lid})](https://boxd.it/${user.lid})\n`;
            try {
                // Attempt to set description, and catch any errors
                this.createEmbedBuilder().setDescription(nextString);
                resultString = nextString;
            } catch (e) {
                // If set desciption throws errors add the previous result to the output
                result.push(this.createEmbedBuilder().setDescription(resultString));
                resultString = '';
            }
        }

        // If there is anything left over add that as a message as well.
        if (resultString) {
            result.push(this.createEmbedBuilder().setDescription(resultString));
        }

        return result;
    }

    createEmptyFollowingEmbed() {
        const description = this.formatDescription('Not following any accounts.');
        return this.createEmbedBuilder().setDescription(description);
    }

    /**
     * @param {import("../models/diary-entry.mjs")} entry
     * @param {import("../models/user.mjs")} data
     */
    createDiaryEntryEmbed(entry, data) {
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
        const description = this.formatDescription(reviewTitle + rule + reviewText);

        const embed = this.createEmbedBuilder()
            .setAuthor({ name: authorTitle, iconURL: profileImage, url: profileURL })
            .setTitle(adult + entry.filmTitle + ' ' + releaseYear)
            .setURL(entry.link)
            .setThumbnail(entry.image || null)
            .setDescription(description);

        // If there is footer data with actual data then include it.
        if (data.footer?.text || data.footer?.icon) {
            embed.setFooter({ text: data.footer.text, iconURL: data.footer.icon });
        }

        return embed;
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-member.mjs')} member
     * @param {import('../models/letterboxd/letterboxd-entry.mjs')[]} entryList
     * @returns {EmbedBuilder}
     */
    createDiaryListEmbed(member, entryList) {
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
        const description = this.formatDescription(entryTextList.join('\n'));

        return this.createEmbedBuilder()
            .setTitle(`Recent Diary Activity from ${member.displayName}`)
            .setURL(`https://boxd.it/${member.id}`)
            .setThumbnail(largestImage.url)
            .setDescription(description);
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-film.mjs')} film
     * @param {import('../models/letterboxd/letterboxd-film-statistics.mjs')} filmStatistics
     * @returns {EmbedBuilder}
     */
    createFilmEmbed(film, filmStatistics) {
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
        description = this.formatDescription(description);

        const largestImage = (film?.poster?.sizes || []).reduce(
            (previous, current) => (current.height || 0 > previous.height ? current : previous),
            {},
        );

        return this.createEmbedBuilder()
            .setTitle(`${film.name} (${film.releaseYear})`)
            .setURL(`https://boxd.it/${film.id}`)
            .setThumbnail(largestImage?.url || '')
            .setDescription(description);
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-member.mjs')} member
     * @param {import('../models/letterboxd/letterboxd-member-statistics.mjs')} memberStatistics
     * @returns {EmbedBuilder}
     */
    createUserEmbed(member, memberStatistics) {
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
        description = this.formatDescription(description);

        const pronounList = [
            member.pronoun.subjectPronoun,
            member.pronoun.objectPronoun,
            member.pronoun.possessivePronoun,
        ];

        return this.createEmbedBuilder()
            .setTitle(`${member.displayName} (${pronounList.join('/')})`)
            .setURL(`https://boxd.it/${member.id}`)
            .setThumbnail(this.parseImage(member?.avatar?.sizes))
            .setDescription(description);
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-list-summary.mjs')} listSummary
     * @returns {EmbedBuilder}
     */
    createListEmbed(listSummary) {
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
        description = this.formatDescription(description);

        return this.createEmbedBuilder()
            .setTitle(listSummary.name)
            .setURL(`https://boxd.it/${listSummary.id}`)
            .setThumbnail(this.parseImage(listSummary?.previewEntries[0]?.film?.poster?.sizes))
            .setDescription(description);
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-log-entry.mjs')[]} logEntryList
     * @returns {EmbedBuilder}
     */
    createLoggedEmbed(logEntryList) {
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
        const description = this.formatDescription(logEntryTextList.join('\n'));

        return this.createEmbedBuilder()
            .setTitle(title)
            .setThumbnail(this.parseImage(logEntryList[0]?.film?.poster?.sizes))
            .setDescription(description);
    }

    /**
     * @param {import('../models/letterboxd/letterboxd-contributor.mjs')} contributor
     * @returns {EmbedBuilder}
     */
    createContributorEmbed(contributor) {
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

        const description = this.formatDescription(filmographyString + '\n' + linkString);
        return this.createEmbedBuilder()
            .setTitle(contributor.name)
            .setURL(`https://boxd.it/${contributor.id}`)
            .setDescription(description);
    }

    /**
     * @param {import('../config.mjs')} config
     * @param {number} userCount
     * @param {number} serverCount
     * @returns
     */
    createHelpEmbed(config, userCount, serverCount) {
        const name = config.get('packageName');
        const version = config.get('packageVersion');
        const description = this.formatDescription(
            `${name} v${version}\nTracking ${userCount} users on ${serverCount} servers`,
        );

        return this.createEmbedBuilder()
            .setTitle('(Help!) I Need Somebody')
            .setURL('https://jimlind.github.io/filmlinkd/')
            .setDescription(description)
            .addFields(
                { name: '/help', value: 'Shows this message' },
                { name: '/follow account [channel]', value: 'Listens for new entries' },
                { name: '/unfollow account [channel]', value: 'Stops listening for new entries' },
                { name: '/following', value: 'List all users followed in this channel' },
                {
                    name: '/refresh account',
                    value: 'Refreshes the Filmlinkd cache for the account',
                },
                {
                    name: '/contributor contributor-name',
                    value: "Shows a film contributor's information",
                },
                { name: '/diary account', value: "Shows a user's 5 most recent entries" },
                { name: '/film film-name', value: "Shows a film's information" },
                { name: '/list account list-name', value: "Shows a users's list's information" },
                { name: '/logged account film-name', value: "Shows a user's entries for a film" },
                { name: '/roulette', value: 'Shows random film information' },
                { name: '/user account', value: "Shows a users's information" },
            )
            .addFields(
                {
                    name: ':clap: Patreon',
                    value: '[Support on Patreon](https://www.patreon.com/filmlinkd)',
                    inline: true,
                },
                {
                    name: ':coffee: Ko-fi',
                    value: '[Support on Ko-fi](https://ko-fi.com/filmlinkd)',
                    inline: true,
                },
                {
                    name: ':left_speech_bubble: Discord',
                    value: '[Join the Discord](https://discord.gg/deZ7EUguge)',
                    inline: true,
                },
            );
    }

    createChannelNotFoundEmbed() {
        const description = this.formatDescription('Unable to find the specified channel.');
        return this.createEmbedBuilder().setDescription(description);
    }

    createFilmNotFoundEmbed() {
        const description = this.formatDescription('Unable to match a film to those search terms.');
        return this.createEmbedBuilder().setDescription(description);
    }

    createContributorNotFoundEmbed() {
        const description = this.formatDescription(
            'Unable to match a contributor to those search terms.',
        );
        return this.createEmbedBuilder().setDescription(description);
    }

    createNoListFoundEmbed() {
        const description = this.formatDescription(
            'Unable to match an account list to those search terms.',
        );
        return this.createEmbedBuilder().setDescription(description);
    }

    createNoLoggedEntriesFoundEmbed() {
        const description = this.formatDescription(
            'Unable to find that film logged for that user.',
        );
        return this.createEmbedBuilder().setDescription(description);
    }

    createEmbedBuilder() {
        return new this.discordLibrary.EmbedBuilder().setColor(0xa700bd);
    }

    /**
     * @param {string} description
     * @returns string
     */
    formatDescription(description) {
        return description.substring(0, 4096);
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
     * @param {import('../models/letterboxd/letterboxd-image-size.mjs')[]} sizes
     * return string
     */
    parseImage(sizes) {
        const findLargest = (previous, current) =>
            current.height || 0 > previous.height ? current : previous;
        const largestImage = (sizes || []).reduce(findLargest, {});
        return largestImage?.url || '';
    }
}
