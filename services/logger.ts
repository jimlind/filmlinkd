'use strict';

class Logger {

    winstonLogger;

    constructor(config, winston, googleCloudWinstonTransport, consoleTransport) {
        // Create a Winston logger that logs debug and above
        this.winstonLogger = winston.createLogger({
            level: 'debug',
            format: winston.format.combine(
                winston.format.timestamp(),
                winston.format.prettyPrint(),
            ),
            transports: [googleCloudWinstonTransport],
        });

        // If the environment is dev, then also log to console
        if (config.getIsDev()) {
            this.winstonLogger.add(consoleTransport);
        }
    }

    debug(...args) {
        this.winstonLogger.debug(...args);
    }
    info(...args) {
        this.winstonLogger.info(...args);
    }
    warn(...args) {
        this.winstonLogger.warn(...args);
    }
    error(...args) {
        this.winstonLogger.error(...args);
    }
}

module.exports = Logger;
