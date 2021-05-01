'use strict';

class Logger {

    winstonLogger: any;

    constructor(config: any, winston: any, googleCloudWinstonTransport: any, consoleTransport: any) {
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

    debug(...args: any[]) {
        this.winstonLogger.debug(...args);
    }
    info(...args: any[]) {
        this.winstonLogger.info(...args);
    }
    warn(...args: any[]) {
        this.winstonLogger.warn(...args);
    }
    error(...args: any[]) {
        this.winstonLogger.error(...args);
    }
}

module.exports = Logger;
