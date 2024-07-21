import { EmbedBuilder } from 'discord.js';
import { LRUCache } from 'lru-cache';
import EmbedBuilderFactory from '../../factories/embed-builder-factory.js';
import DiaryEntry from '../../models/diary-entry.js';
import User from '../../models/user.js';
import DiscordMessageSender from '../discord/discord-message-sender.js';
import FirestoreUserDao from '../google/firestore/firestore-user-dao.js';
import LetterboxdLidComparison from '../letterboxd/letterboxd-lid-comparison.js';
import Logger from '../logger.js';

/**
 * Class dealing with writing diary entry events to Discord servers
 */
export default class DiaryEntryWriter {
    constructor(
        readonly cache: typeof LRUCache.prototype,
        readonly discordMessageSender: DiscordMessageSender,
        readonly embedBuilderFactory: EmbedBuilderFactory,
        readonly firestoreUserDao: FirestoreUserDao,
        readonly letterboxdLidComparison: LetterboxdLidComparison,
        readonly logger: Logger,
    ) {}

    async validateAndWrite(
        diaryEntry: DiaryEntry,
        channelIdOverride: string,
    ): Promise<void | User> {
        // We are expecting multiple requests to post a diary entry so we maintain the one source of
        // truth on the server that sends messages. We keep an in memory cache.
        if (!channelIdOverride && this.cache.get(diaryEntry.lid)) {
            return;
        } else {
            // Assume that write will succeed so write to cache. If it actually doesn't succed then it'll
            // get tried again some later time, but this is designed to limit duplicates from scrape events
            this.cache.set(diaryEntry.lid, true);
            this.logger.info('LRU Cache Size', { calculatedSize: this.cache.size });
        }

        // Load the userModel from the database
        const userModel = await this.firestoreUserDao.getByLetterboxdId(diaryEntry.userLid);

        // Exit early if user not found
        if (!userModel) {
            return;
        }
        // Exit early if no subscribed channels
        if ((userModel?.channelList || []).length === 0) {
            return;
        }
        // Exit early if it is an adult film (maybe a future feature)
        if (diaryEntry?.adult) {
            return;
        }

        // Double check that the entry is newer than what was stored in the database
        // Ignore this check if there is a channel override because we want it to trigger multiple times.
        const entryComparison = this.letterboxdLidComparison.compare(
            userModel?.previous?.lid || '',
            diaryEntry.lid,
        );
        if (!channelIdOverride && entryComparison !== 1) {
            return;
        }

        // Rewrite the channel list if there is an override sent
        const channelList = [{ channelId: channelIdOverride }];
        const sendingUser = channelIdOverride ? { ...userModel, channelList } : userModel;

        var embed = new EmbedBuilder();
        try {
            embed = this.embedBuilderFactory.createDiaryEntryEmbed(diaryEntry, sendingUser);
        } catch (error) {
            const message = `Creating Diary Entry Embed for '${diaryEntry?.filmTitle}' by '${diaryEntry?.userName}' failed.`;
            this.logger.warn(message);
            return;
        }

        for (const channel of userModel.channelList) {
            try {
                await this.discordMessageSender.send(channel.channelId, embed);
            } catch (error) {
                const logData = { error, diaryEntry, channelIdOverride };
                this.logger.warn(
                    `Entry for '${diaryEntry?.filmTitle}' by '${diaryEntry?.userName}' did not validate and write.`,
                    logData,
                );
            }
        }

        return userModel;
    }
}
