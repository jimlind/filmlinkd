import DiaryEntry from '../models/diary-entry.js';

export default class DiaryEntryFactory {
    /**
     * @param {string} inputString
     * @return {import('../models/diary-entry')}
     */
    createFromMessage(message) {
        const messageString = message?.data?.toString() || '';
        const jsonObject = JSON.parse(messageString);

        return Object.assign(new DiaryEntry(), jsonObject?.entry);
    }
}
