package com.itea.agent;

public class Logger {
    private static Logger logger = null;

    public Logger() {
    }

    public static Logger getLogger() {
        if (logger == null) {
            logger = new Logger();
        }
        return logger;
    }

    public void info(String s) {
        System.out.println(s);
    }

    public void debug(String message) {
        System.out.println(message);
    }
}
