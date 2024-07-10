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
        readonly letterboxdFilmFactory: any,
        readonly letterboxdImageFactory: any,
        readonly letterboxdLinkFactory: any,
        readonly letterboxdPronounFactory: any,
    ) {}

    /**
     * @param {Object} memberData
     * @returns LetterboxdMember
     */
    buildMemberFromObject(memberData: any) {
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
        letterboxdMember.favoriteFilms = (memberData.favoriteFilms || []).map((filmSummary: any) =>
            this.letterboxdFilmFactory.buildFilmSummaryFromObject(filmSummary),
        );
        // TODO: Pinned Reviews
        letterboxdMember.pinnedReviews = [];
        letterboxdMember.links = (memberData.links || []).map((link: any) =>
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
    buildMemberIdentifierFromObject(memberIdentifierData: any) {
        const memberIdentifier = new LetterboxdMemberIdentifier();

        memberIdentifier.id = memberIdentifierData.id;

        return memberIdentifier;
    }

    /**
     * @param {Object} memberStatisticsData
     * @returns LetterboxdMemberStatistics
     */
    buildMemberStatisticsFromObject(memberStatisticsData: any) {
        const memberStatistics = new LetterboxdMemberStatistics();

        memberStatistics.member = this.buildMemberIdentifierFromObject(
            memberStatisticsData.member,
        ) as any;
        memberStatistics.counts = this.buildMemberStatisticsCountsFromObject(
            memberStatisticsData.counts,
        ) as any;
        // TODO: Ratings Histogram
        memberStatistics.ratingsHistogram = [];
        memberStatistics.yearsInReview = memberStatisticsData.yearsInReviewl;

        return memberStatistics;
    }

    /**
     * @param {Object} memberStatisticsCountsData
     * @returns LetterboxdMemberStatisticsCounts
     */
    buildMemberStatisticsCountsFromObject(memberStatisticsCountsData: any) {
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
    buildMemberSummaryFromObject(memberSummaryData: any) {
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
