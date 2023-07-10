import LetterboxdMemberIdentifier from '../../models/letterboxd/letterboxd-member-identifier.js';
import LetterboxdMemberStatisticsCounts from '../../models/letterboxd/letterboxd-member-statistics-counts.js';
import LetterboxdMemberStatistics from '../../models/letterboxd/letterboxd-member-statistics.js';
import LetterboxdMemberSummary from '../../models/letterboxd/letterboxd-member-summary.js';
import LetterboxdMember from '../../models/letterboxd/letterboxd-member.js';

export default class LetterboxdMemberFactory {
    /**
     * @param {import('./letterboxd-film-factory.mjs')} letterboxdFilmFactory
     * @param {import('./letterboxd-image-factory.mjs')} letterboxdImageFactory
     * @param {import('./letterboxd-link-factory.mjs')} letterboxdLinkFactory
     * @param {import('./letterboxd-pronoun-factory.mjs')} letterboxdPronounFactory
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

    /**
     * @param {Object} memberIdentifierData
     * @returns LetterboxdMemberIdentifier
     */
    buildMemberIdentifierFromObject(memberIdentifierData) {
        const memberIdentifier = new LetterboxdMemberIdentifier();

        memberIdentifier.id = memberIdentifierData.id;

        return memberIdentifier;
    }

    /**
     * @param {Object} memberStatisticsData
     * @returns LetterboxdMemberStatistics
     */
    buildMemberStatisticsFromObject(memberStatisticsData) {
        const memberStatistics = new LetterboxdMemberStatistics();

        memberStatistics.member = this.buildMemberIdentifierFromObject(memberStatisticsData.member);
        memberStatistics.counts = this.buildMemberStatisticsCountsFromObject(
            memberStatisticsData.counts,
        );
        // TODO: Ratings Histogram
        memberStatistics.ratingsHistogram = [];
        memberStatistics.yearsInReview = memberStatisticsData.yearsInReviewl;

        return memberStatistics;
    }

    /**
     * @param {Object} memberStatisticsCountsData
     * @returns LetterboxdMemberStatisticsCounts
     */
    buildMemberStatisticsCountsFromObject(memberStatisticsCountsData) {
        const memberStatisticsCounts = new LetterboxdMemberStatisticsCounts();

        memberStatisticsCounts.filmLikes = memberStatisticsCountsData.filmLikes;
        memberStatisticsCounts.listLikes = memberStatisticsCountsData.listLikes;
        memberStatisticsCounts.reviewLikes = memberStatisticsCountsData.reviewLikes;
        memberStatisticsCounts.watches = memberStatisticsCountsData.watches;
        memberStatisticsCounts.ratings = memberStatisticsCountsData.ratings;
        memberStatisticsCounts.reviews = memberStatisticsCountsData.reviews;
        memberStatisticsCounts.diaryEntries = memberStatisticsCountsData.diaryEntries;
        memberStatisticsCounts.diaryEntriesThisYear =
            memberStatisticsCountsData.diaryEntriesThisYear;
        memberStatisticsCounts.filmsInDiaryThisYear =
            memberStatisticsCountsData.filmsInDiaryThisYear;
        memberStatisticsCounts.watchlist = memberStatisticsCountsData.watchlist;
        memberStatisticsCounts.lists = memberStatisticsCountsData.lists;
        memberStatisticsCounts.unpublishedLists = memberStatisticsCountsData.unpublishedLists;
        memberStatisticsCounts.followers = memberStatisticsCountsData.followers;
        memberStatisticsCounts.following = memberStatisticsCountsData.following;
        memberStatisticsCounts.listTags = memberStatisticsCountsData.listTags;
        memberStatisticsCounts.filmTags = memberStatisticsCountsData.filmTags;

        return memberStatisticsCounts;
    }

    /**
     * @param {Object} memberSummaryData
     * @returns LetterboxdMemberSummary
     */
    buildMemberSummaryFromObject(memberSummaryData) {
        const letterboxdMemberSummary = new LetterboxdMemberSummary();

        letterboxdMemberSummary.id = memberSummaryData.id || '';
        letterboxdMemberSummary.userName = (memberSummaryData.username || '').toLowerCase();
        letterboxdMemberSummary.givenName = memberSummaryData.givenName || '';
        letterboxdMemberSummary.familyName = memberSummaryData.familyName || '';
        letterboxdMemberSummary.displayName = memberSummaryData.displayName || '';
        letterboxdMemberSummary.shortName = memberSummaryData.shortName || '';
        letterboxdMemberSummary.pronoun = this.letterboxdPronounFactory.buildPronounFromObject(
            memberSummaryData.pronoun || {},
        );
        letterboxdMemberSummary.avatar = this.letterboxdImageFactory.buildImageFromObject(
            memberSummaryData.avatar || {},
        );
        letterboxdMemberSummary.memberStatus = memberSummaryData.memberStatus || '';
        letterboxdMemberSummary.hideAdsInContent = memberSummaryData.hideAdsInContent || false;
        letterboxdMemberSummary.commentPolicy = memberSummaryData.commentPolicy || '';
        letterboxdMemberSummary.accountStatus = memberSummaryData.accountStatus || '';
        letterboxdMemberSummary.hideAds = memberSummaryData.hideAds || false;

        return letterboxdMemberSummary;
    }
}
