package com.charity_hub.domain.contracts;

public interface ILogger {
    void info(String message);
    void log(String message);
    void errorLog(String message);
    void warn(String message);
    void error(String message, Exception e);
}