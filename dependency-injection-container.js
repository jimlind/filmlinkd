"use strict";

const awilix = require("awilix");
const discord = require("discord.js");
const { LetterboxdDiary, LetterboxdProfile } = require("letterboxd");
const { LoggingWinston } = require("@google-cloud/logging-winston");
const winston = require("winston");

class DependencyInjectionContainer {
    constructor(configModel) {
        this.container = awilix.createContainer();

        // Create logger transport for the GCP console
        const googleCloudWinstonTransport = new LoggingWinston({
            labels: {
                app: configModel.packageName,
                version:
                    configModel.packageVersion +
                    (configModel.isDev ? "-dev" : ""),
            },
            prefix: configModel.isDev ? "DEV" : null,
            projectId: configModel.googleCloudProjectId,
            keyFilename: configModel.gcpKeyFile,
        });

        this.container.register({
            config: awilix.asValue(configModel),
            discordClient: awilix.asClass(discord.Client).classic(),
            googleCloudWinstonTransport: awilix.asValue(
                googleCloudWinstonTransport
            ),
            letterboxdDiary: awilix.asValue(LetterboxdDiary),
            letterboxdProfile: awilix.asValue(LetterboxdProfile),
            winston: awilix.asValue(winston),
        });

        this.container.loadModules(["factories/**/*.js", "services/**/*.js"], {
            formatName: "camelCase",
            resolverOptions: {
                lifetime: awilix.Lifetime.SINGLETON,
                injectionMode: awilix.InjectionMode.CLASSIC,
            },
        });
    }

    resolve(connectionString) {
        return this.container.resolve(connectionString);
    }
}

module.exports = DependencyInjectionContainer;
