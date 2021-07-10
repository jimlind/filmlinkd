class User {
    /** @property {string} id Firestore document id */
    id = '';
    /** @property {number} created Unix timestamp for document creation */
    created = 0;
    /** @property {string} userName Letterboxd user name */
    userName = '';
    /** @property {number} updated When the document was last updated */
    updated = 0;
    /** @property {number} checked When the Letterboxd account was last updated */
    checked = 0;
    /** @property {{id: number; uri: string; published: number}} previous Previous diary entry posted */
    previous = {};
    /** @property {{guildId: string; channelId: string;}[]} channelList All channels subscribed to user */
    channelList = [];
    /** @property {string} image URL for user profile image */
    image = '';
    /** @property {string} displayName Letterboxd account display name */
    displayName = '';
}

module.exports = User;
