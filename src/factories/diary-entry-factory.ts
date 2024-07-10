import DiaryEntry from '../models/diary-entry.js';

export default class DiaryEntryFactory {
    /**
     * @return {import('../models/diary-entry.mjs')}
     */
    create() {
        return new DiaryEntry();
    }

    /**
     * @param {string} inputString
     * @return {import('../models/diary-entry.mjs')}
     */
    createFromMessage(message: any) {
        const messageString = message?.data?.toString() || '';
        const jsonObject = JSON.parse(messageString);

        return Object.assign(new DiaryEntry(), jsonObject?.entry);
    }
}
