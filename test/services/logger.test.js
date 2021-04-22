const describe = require('mocha').describe;
const sinon = require('sinon');

const Config = require('../../models/config');
const crypto = require('crypto');
const Logger = require('../../services/logger');
const { LoggingWinston } = require('@google-cloud/logging-winston');
const winston = require('winston');

describe('Logger', () => {
    afterEach(() => {
        sinon.restore();
    });

    it('should configure a winston logger', () => {
        const { winstonStub } = setupWinstonStub();

        var timestampValue = crypto.randomBytes(16).toString('hex');
        winstonStub.format.timestamp.returns(timestampValue);

        var prettyPrintValue = crypto.randomBytes(16).toString('hex');
        winstonStub.format.prettyPrint.returns(prettyPrintValue);

        var combineValue = crypto.randomBytes(16).toString('hex');
        winstonStub.format.combine.returns(combineValue);

        const winstonTransportStub = setupWinstonTransportStub();
        const logger = new Logger(
            setupConfigStub(),
            winstonStub,
            winstonTransportStub,
            setupConsoleTransportStub(),
        );

        sinon.assert.calledOnce(winstonStub.format.timestamp);
        sinon.assert.calledOnce(winstonStub.format.prettyPrint);
        sinon.assert.calledOnce(winstonStub.format.combine);
        sinon.assert.calledWith(winstonStub.format.combine, timestampValue, prettyPrintValue);

        sinon.assert.calledOnce(winstonStub.createLogger);
        sinon.assert.calledWith(winstonStub.createLogger, {
            level: 'debug',
            format: combineValue,
            transports: [winstonTransportStub],
        });
    });

    it('should configure a winston logger for dev with console logger', function () {
        const { winstonStub, loggerMethods } = setupWinstonStub();

        configStub = setupConfigStub();
        configStub.getIsDev.returns(true);

        consoleTransportStub = setupConsoleTransportStub();

        const logger = new Logger(
            configStub,
            winstonStub,
            setupWinstonTransportStub(),
            consoleTransportStub,
        );

        sinon.assert.calledOnce(configStub.getIsDev);
        sinon.assert.calledOnce(loggerMethods.add);
        sinon.assert.calledWith(loggerMethods.add, consoleTransportStub);
    });

    it('should configure a winston logger for prod without additional loggers', function () {
        const { winstonStub, loggerMethods } = setupWinstonStub();

        configStub = setupConfigStub();
        configStub.getIsDev.returns(false);

        const logger = new Logger(
            configStub,
            winstonStub,
            setupWinstonTransportStub(),
            setupConsoleTransportStub(),
        );

        sinon.assert.calledOnce(configStub.getIsDev);
        sinon.assert.notCalled(loggerMethods.add);
    });

    it('should pass debug arguments', function () {
        const { winstonStub, loggerMethods } = setupWinstonStub();
        const logger = new Logger(
            setupConfigStub(),
            winstonStub,
            setupWinstonTransportStub(),
            setupConsoleTransportStub(),
        );

        const arguments = [
            crypto.randomBytes(16).toString('hex'),
            crypto.randomBytes(16).toString('hex'),
            crypto.randomBytes(16).toString('hex'),
        ];
        logger.debug(...arguments);

        sinon.assert.calledOnce(loggerMethods.debug);
        sinon.assert.notCalled(loggerMethods.info);
        sinon.assert.notCalled(loggerMethods.warn);
        sinon.assert.notCalled(loggerMethods.error);

        sinon.assert.calledWith(loggerMethods.debug, ...arguments);
    });

    it('should pass info arguments', function () {
        const { winstonStub, loggerMethods } = setupWinstonStub();
        const logger = new Logger(
            setupConfigStub(),
            winstonStub,
            setupWinstonTransportStub(),
            setupConsoleTransportStub(),
        );

        const arguments = [
            crypto.randomBytes(16).toString('hex'),
            crypto.randomBytes(16).toString('hex'),
            crypto.randomBytes(16).toString('hex'),
        ];
        logger.info(...arguments);

        sinon.assert.notCalled(loggerMethods.debug);
        sinon.assert.calledOnce(loggerMethods.info);
        sinon.assert.notCalled(loggerMethods.warn);
        sinon.assert.notCalled(loggerMethods.error);

        sinon.assert.calledWith(loggerMethods.info, ...arguments);
    });

    it('should pass warn arguments', function () {
        const { winstonStub, loggerMethods } = setupWinstonStub();
        const logger = new Logger(
            setupConfigStub(),
            winstonStub,
            setupWinstonTransportStub(),
            setupConsoleTransportStub(),
        );

        const arguments = [
            crypto.randomBytes(16).toString('hex'),
            crypto.randomBytes(16).toString('hex'),
            crypto.randomBytes(16).toString('hex'),
        ];
        logger.warn(...arguments);

        sinon.assert.notCalled(loggerMethods.debug);
        sinon.assert.notCalled(loggerMethods.info);
        sinon.assert.calledOnce(loggerMethods.warn);
        sinon.assert.notCalled(loggerMethods.error);

        sinon.assert.calledWith(loggerMethods.warn, ...arguments);
    });

    it('should pass error arguments', function () {
        const { winstonStub, loggerMethods } = setupWinstonStub();
        const logger = new Logger(
            setupConfigStub(),
            winstonStub,
            setupWinstonTransportStub(),
            setupConsoleTransportStub(),
        );

        const arguments = [
            crypto.randomBytes(16).toString('hex'),
            crypto.randomBytes(16).toString('hex'),
            crypto.randomBytes(16).toString('hex'),
        ];
        logger.error(...arguments);

        sinon.assert.notCalled(loggerMethods.debug);
        sinon.assert.notCalled(loggerMethods.info);
        sinon.assert.notCalled(loggerMethods.warn);
        sinon.assert.calledOnce(loggerMethods.error);

        sinon.assert.calledWith(loggerMethods.error, ...arguments);
    });
});

setupConfigStub = () => {
    return sinon.createStubInstance(Config);
};

setupWinstonTransportStub = () => {
    return sinon.createStubInstance(LoggingWinston);
};

setupConsoleTransportStub = () => {
    return sinon.createStubInstance(winston.transports.Console);
};

setupWinstonStub = () => {
    winstonStub = sinon.stub(winston);
    winstonStub.format = {
        timestamp: sinon.stub(),
        prettyPrint: sinon.stub(),
        combine: sinon.stub(),
    };

    loggerMethods = {
        add: sinon.stub(),
        debug: sinon.stub(),
        info: sinon.stub(),
        warn: sinon.stub(),
        error: sinon.stub(),
    };
    winstonStub.createLogger.returns(loggerMethods);

    return { winstonStub, loggerMethods };
};
