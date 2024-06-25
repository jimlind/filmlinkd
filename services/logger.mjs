export default class Logger {
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
        if (!config.get('live')) {
            this.winstonLogger.add(consoleTransport);
        }
    }

    debug(...args) {
        return this.winstonLogger.debug(...args);
    }
    info(...args) {
        return this.winstonLogger.info(...args);
    }
    warn(...args) {
        return this.winstonLogger.warn(...args);
    }
    error(...args) {
        return this.winstonLogger.error(...args);
    }
}
