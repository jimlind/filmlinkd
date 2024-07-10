/**
 * Letterboxd API Member Object
 * https://api-docs.letterboxd.com/#/definitions/Member
 */

export default class LetterboxdMember {
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
    /** @property {import('./letterboxd-image} avatar The member’s avatar image at multiple sizes. Avatar images to not have an enforced aspect ratio, so should be center-cropped to a square if they are not 1:1. */
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
    /** @property {string} twitterUsername The member’s Twitter username, if they have authenticated their account. */
    twitterUsername = '';
    /** @property {string} bioLbml The member’s bio in LBML. May contain the following HTML tags: <br> <strong> <em> <b> <i> <a href=""> <blockquote>. */
    bioLbml = '';
    /** @property {string} location The member’s location. */
    location = '';
    /** @property {string} website The member’s website URL. URLs are not validated, so sanitizing may be required. */
    website = '';
    /** @property {string} backdrop The member’s backdrop image at multiple sizes, sourced from the first film in the member’s list of favorite films, if available. Only returned for Patron members. */
    backdrop = '';
    /** @property {number} backdropFocalPoint The vertical focal point of the member’s backdrop image, if available. Expressed as a proportion of the image’s height, using values between 0.0 and 1.0. Use when cropping the image into a shorter space, such as in the page for a film on the Letterboxd site. */
    backdropFocalPoint = 0;
    /** @property {import('./letterboxd-film-summary')[]}  favoriteFilms A summary of the member’s favorite films, up to a maximum of four. */
    favoriteFilms = [];
    // TODO: Define this properly
    /** @property {*[]} pinnedReviews The reviews the member has pinned on their profile page, up to a maximum of two. Only returned for paying members. */
    pinnedReviews = [];
    /** @property {import('./letterboxd-link')[]} links A link to the member’s profile page on the Letterboxd website. */
    links = [];
    /** @property {boolean} privateWatchlist Defaults to false for new accounts. Indicates whether the member has elected to hide their watchlist from other members. */
    privateWatchlist = false;
    // TODO: Define this properly
    /** @property {*} featuredList A summary of the member’s featured list. Only returned for HQ members. */
    featuredList = null;
    // TODO: Define this properly
    /** @property {*[]} teamMembers A summary of the member’s team members. Only returned for HQ members. */
    teamMembers = [];
    /** @property {string} bio The member’s bio formatted as HTML. */
    bio = '';
}
