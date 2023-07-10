/**
 * Letterboxd API Member Statistics Counts Object
 * https://api-docs.letterboxd.com/#/definitions/MemberStatisticsCounts
 */

export default class LetterboxdMemberStatisticsCounts {
    /** @property {number} filmLikes The number of films the member has liked. */
    filmLikes = 0;
    /** @property {number} listLikes The number of lists the member has liked. */
    listLikes = 0;
    /** @property {number} reviewLikes The number of reviews the member has liked. */
    reviewLikes = 0;
    /** @property {number} watches The number of films the member has watched. This is a distinct total — films with multiple log entries are only counted once. */
    watches = 0;
    /** @property {number} ratings The number of films the member has rated. */
    ratings = 0;
    /** @property {number} reviews The number of films the member has reviewed. */
    reviews = 0;
    /** @property {number} diaryEntries The number of entries the member has in their diary. */
    diaryEntries = 0;
    /** @property {number} diaryEntriesThisYear The number of entries the member has in their diary for the current year. The current year rolls over at midnight on 31 December in New Zealand Daylight Time (GMT + 13). */
    diaryEntriesThisYear = 0;
    /** @property {number} filmsInDiaryThisYear The number of unique films the member has in their diary for the current year. The current year rolls over at midnight on 31 December in New Zealand Daylight Time (GMT + 13). */
    filmsInDiaryThisYear = 0;
    /** @property {number} watchlist The number of films the member has in their watchlist. */
    watchlist = 0;
    /** @property {number} lists The number of lists for the member. Includes unpublished lists if the request is made for the authenticated member. */
    lists = 0;
    /** @property {number} unpublishedLists The number of unpublished lists for the member. Only included if the request is made for the authenticated member. */
    unpublishedLists = 0;
    /** @property {number} followers The number of members who follow the member. */
    followers = 0;
    /** @property {number} following The number of members the member is following. */
    following = 0;
    /** @property {number} listTags The number of tags the member has used for lists. */
    listTags = 0;
    /** @property {number} filmTags The number of tags the member has used for diary entries and reviews. */
    filmTags = 0;
}
