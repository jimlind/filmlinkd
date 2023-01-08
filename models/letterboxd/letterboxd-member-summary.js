/**
 * Letterboxd API MemberSummary Object
 * https://api-docs.letterboxd.com/#/definitions/MemberSummary
 */

class LetterboxdMemberSummary {
    /** @property {string} id The LID of the member. */
    id = '';
    /** @property {string} userName The member’s Letterboxd username. Usernames must be between 2 and 15 characters long and may only contain upper or lowercase letters, numbers or the underscore (_) character. */
    userName = '';
    /** @property {string} givenName The given name of the member. */
    givenName = '';
    /** @property {string} familyName The family name of the member. */
    familyName = '';
    /** @property {string} displayName A convenience method that returns the member’s given name and family name concatenated with a space, if both are set, or just their given name or family name, if one is set, or their username, if neither is set. Will never be empty. */
    displayName = '';
    /** @property {string} shortName A convenience method that returns the member’s given name, if set, or their username. Will never be empty. */
    shortName = '';
    /** @property {import('./letterboxd-pronoun')} pronoun The member’s preferred pronoun. Use the /members/pronouns endpoint to request all available pronouns. */
    pronoun = null;
    /** @property {{sizes: import('./letterboxd-image-size')[]}} avatar The member’s avatar image at multiple sizes. Avatar images to not have an enforced aspect ratio, so should be center-cropped to a square if they are not 1:1. */
    avatar = null;
    // TODO: memberStatus is an enum
    /** @property {string} memberStatus The member’s account type. */
    memberStatus = '';
    /** @property {boolean} hideAdsInContent true if ads should not be shown on the member's content. */
    hideAdsInContent = false;
    // TODO: commentPolicy is an enum
    /** @property {string} commentPolicy The member’s default policy determing who can post comments to their content. Supported options are Anyone, Friends and You. You in this context refers to the content owner. Use the commentThreadState property of the ListRelationship to determine the signed-in member’s ability to comment (or not). */
    commentPolicy = '';
    // TODO: accountStatus is an enum
    /** @property {string} accountStatus The member’s account status. */
    accountStatus = '';
    /** @property {boolean} hideAds true if member should not be shown ads. */
    hideAds = false;
}

module.exports = LetterboxdMemberSummary;
