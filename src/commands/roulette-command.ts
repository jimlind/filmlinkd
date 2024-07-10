export default class RouletteCommand {
    /**
     * @param {import('../http-client')} httpClient
     * @param {import('../services/letterboxd/api/letterboxd-film-api.mjs')} letterboxdFilmApi
     * @param {import('../services/letterboxd/letterboxd-lid-web.mjs')} letterboxdLidWeb
     * @param {import('../factories/embed-builder-factory.mjs')} embedBuilderFactory
     */
    constructor(
        readonly httpClient: any,
        readonly letterboxdFilmApi: any,
        readonly letterboxdLidWeb: any,
        readonly embedBuilderFactory: any,
    ) {}

    /**
     * @returns {import('discord.js').EmbedBuilder}
     */
    getEmbed() {
        return this.getRandomFilmPath()
            .then((path: any) => this.letterboxdLidWeb.getFromPath(path))
            .then((lid: any) =>
                Promise.all([
                    this.letterboxdFilmApi.getFilm(lid),
                    this.letterboxdFilmApi.getFilmStatistics(lid),
                ]),
            )
            .then(([film, filmStatistics]: any) =>
                this.embedBuilderFactory.createFilmEmbed(film, filmStatistics),
            )
            .catch(() => this.embedBuilderFactory.createFilmNotFoundEmbed());
    }

    /**
     *  Get a path for a random film on Letterboxd
     *
     * @param {string} path
     * @returns string
     */
    getRandomFilmPath(path = '') {
        if (path.length < 3) {
            path = this.getRandomLID();
        }

        return this.httpClient
            .get('https://boxd.it/' + path, 50000)
            .then((response: any) => {
                const letterboxdType = response?.headers['x-letterboxd-type'] || '';
                switch (letterboxdType) {
                    case 'Film':
                        return response.request.path;
                    case 'LogEntry':
                        const result = /data-film-link="(.+?)"/.exec(response.data);
                        return '/film/' + result?.[1];
                    default:
                        const resultList = /data-film-slug="(.+?)"/.exec(response.data);
                        if (resultList) {
                            return '/film/' + resultList[1];
                        }
                }
                throw null;
            })
            .catch(() => {
                // Shorten the path by 1 and try again
                return this.getRandomFilmPath(path.substring(1));
            });
    }

    /**
     * Get a random 6 character length string that confirms to the LID style
     * This string is too long so almost gauranteed that it will 404 as-is
     *
     * @returns string
     */
    getRandomLID() {
        return Array(6)
            .fill('')
            .map(() => {
                const x = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
                return x[Math.floor(Math.random() * x.length)];
            })
            .join('');
    }
}
