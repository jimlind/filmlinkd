"use strict";

class DiscordConnection {
    connected = false;
    locked = false;

    constructor(config, discordClient) {
        this.config = config;
        this.discordClient = discordClient;
    }

    getConnectedClient() {
        return new Promise((resolve, reject) => {
            // If no token set reject the request
            if (!this.config.discordBotToken) {
                return reject('No Discord Bot Token Set');
            }

            // If the client is connected return it
            if (this.connected) {
                return resolve(this.discordClient);
            }

            // If the connecting process is happening reject additional attempts
            // Multiple connections means something terrible has happened
            if (this.locked) {
                return reject();
            }

            // Indicate the connecting process is active
            this.locked = true;

            // On ready needs to be setup before login is called or ready events may be missed
            this.discordClient.on("ready", () => {
                this.connected = true;
                this.locked = false;

                return resolve(this.discordClient);
            });
            this.discordClient.login(this.config.discordBotToken);
        });
    }
}

module.exports = DiscordConnection;
