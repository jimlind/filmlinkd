export default class Logger {
    winstonLogger: any;

    constructor(
        readonly config: any,
        readonly winston: any,
        readonly googleCloudWinstonTransport: any,
        readonly consoleTransport: any,
    ) {
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

    debug(...args: any) {
        return this.winstonLogger.debug(...args);
    }
    info(...args: any) {
        return this.winstonLogger.info(...args);
    }
    warn(...args: any) {
        return this.winstonLogger.warn(...args);
    }
    error(...args: any) {
        return this.winstonLogger.error(...args);
    }
}
