package com.charity_hub.infrastructure;

import com.charity_hub.domain.contracts.ILogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SLF4JLogger implements ILogger {
    private final Logger logger;

    public SLF4JLogger(Class<?> clazz) {
        this.logger = LoggerFactory.getLogger(clazz);
    }

    @Override
    public void info(String message) {
        logger.info(message);
    }

    @Override
    public void warn(String message) {
        logger.warn(message);
    }

    @Override
    public void error(String message, Exception e) {
        logger.error(message, e);
    }

    @Override
    public void log(String message) {
        logger.debug(message);

    }

    @Override
    public void errorLog(String message) {
        logger.error(message);
    }
}
