class RouletteCommand {
    /**
     * @param {import('../http-client')} httpClient
     * @param {import('../services/letterboxd/api/letterboxd-film-api')} letterboxdFilmApi
     * @param {import('../services/letterboxd/letterboxd-lid-web')} letterboxdLidWeb
     * @param {import('../factories/embed-builder-factory')} embedBuilderFactory
     */
    constructor(httpClient, letterboxdFilmApi, letterboxdLidWeb, embedBuilderFactory) {
        this.httpClient = httpClient;
        this.letterboxdFilmApi = letterboxdFilmApi;
        this.letterboxdLidWeb = letterboxdLidWeb;
        this.embedBuilderFactory = embedBuilderFactory;
    }

    /**
     * @returns {import('discord.js').EmbedBuilder}
     */
    getMessage() {
        return this.getRandomFilmPath()
            .then((path) => this.letterboxdLidWeb.getFromPath(path))
            .then((lid) =>
                Promise.all([
                    this.letterboxdFilmApi.getFilm(lid),
                    this.letterboxdFilmApi.getFilmStatistics(lid),
                ]),
            )
            .then(([film, filmStatistics]) =>
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
            .then((response) => {
                const letterboxdType = response?.headers['x-letterboxd-type'] || '';
                switch (letterboxdType) {
                    case 'Film':
                        return response.request.path;
                    case 'LogEntry':
                        const result = /data-film-link="(.+?)"/.exec(response.data)[1];
                        return result;
                    default:
                        const resultList = /data-film-slug="(.+?)"/.exec(response.data);
                        if (resultList) {
                            return resultList[1];
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

module.exports = RouletteCommand;
