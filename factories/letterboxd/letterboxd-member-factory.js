const LetterboxdMember = require('../../models/letterboxd/letterboxd-member');

class LetterboxdMemberFactory {
    /**
     * @param {import('./letterboxd-film-factory')} letterboxdFilmFactory
     * @param {import('./letterboxd-image-factory')} letterboxdImageFactory
     * @param {import('./letterboxd-link-factory')} letterboxdLinkFactory
     * @param {import('./letterboxd-pronoun-factory')} letterboxdPronounFactory
     */
    constructor(
        letterboxdFilmFactory,
        letterboxdImageFactory,
        letterboxdLinkFactory,
        letterboxdPronounFactory,
    ) {
        this.letterboxdFilmFactory = letterboxdFilmFactory;
        this.letterboxdImageFactory = letterboxdImageFactory;
        this.letterboxdLinkFactory = letterboxdLinkFactory;
        this.letterboxdPronounFactory = letterboxdPronounFactory;
    }

    /**
     * @param {Object} memberData
     * @returns LetterboxdMember
     */
    buildMemberFromObject(memberData) {
        const letterboxdMember = new LetterboxdMember();

        letterboxdMember.id = memberData.id || '';
        letterboxdMember.userName = (memberData.username || '').toLowerCase();
        letterboxdMember.givenName = memberData.givenName || '';
        letterboxdMember.familyName = memberData.familyName || '';
        letterboxdMember.displayName = memberData.displayName || '';
        letterboxdMember.shortName = memberData.shortName || '';
        letterboxdMember.pronoun = this.letterboxdPronounFactory.buildPronounFromObject(
            memberData.pronoun || {},
        );
        letterboxdMember.avatar = this.letterboxdImageFactory.buildImageFromObject(
            memberData.avatar || {},
        );
        letterboxdMember.memberStatus = memberData.memberStatus || '';
        letterboxdMember.hideAdsInContent = memberData.hideAdsInContent || false;
        letterboxdMember.commentPolicy = memberData.commentPolicy || '';
        letterboxdMember.accountStatus = memberData.accountStatus || '';
        letterboxdMember.hideAds = memberData.hideAds || false;
        letterboxdMember.twitterUsername = memberData.twitterUsername || '';
        letterboxdMember.bioLbml = memberData.bioLbml || '';
        letterboxdMember.location = memberData.location || '';
        letterboxdMember.website = memberData.website || '';
        letterboxdMember.backdrop = this.letterboxdImageFactory.buildImageFromObject(
            memberData.backdrop || {},
        );
        letterboxdMember.backdropFocalPoint = memberData.backdropFocalPoint || 0;
        letterboxdMember.favoriteFilms = (memberData.favoriteFilms || []).map((filmSummary) =>
            this.letterboxdFilmFactory.buildFilmSummaryFromObject(filmSummary),
        );
        // TODO: Pinned Reviews
        letterboxdMember.pinnedReviews = [];
        letterboxdMember.links = (memberData.links || []).map((link) =>
            this.letterboxdLinkFactory.buildLinkFromObject(link),
        );
        letterboxdMember.privateWatchlist = memberData.privateWatchlist || false;
        // TODO: Featured List
        letterboxdMember.featuredList = null;
        // TODO: Team Members
        letterboxdMember.teamMembers = [];
        letterboxdMember.bio = memberData.bio || '';

        return letterboxdMember;
    }
}

module.exports = LetterboxdMemberFactory;
