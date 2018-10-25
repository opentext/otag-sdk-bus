package com.opentext.otag.sdk.bus;

import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * stdout out based logger for the SDK event bus.
 */
public class SdkEventBusLog {

    // a String set top "true" enables file logging
    private static final String SDK_EVENT_LOGGING_ENV_VAR = "AWG_SDK_EVENT_BUS_LOGGING_ENABLED_ENV";

    private static final String LOG_LINE = "%s - INFO - %s";
    private static final String ERR_LOG_LINE = "%s - ERROR - %s %s";

    /**
     * Should this logger actually perform any logging?
     */
    private static boolean writeToLog;

    static {
        boolean shouldLog = false;
        try {
            shouldLog = Boolean.TRUE.toString().equalsIgnoreCase(System.getenv(SDK_EVENT_LOGGING_ENV_VAR));
        } catch (Exception e) {
            System.err.println("Failed to process SDK Event Bus Logging env var - " +
                    e.getMessage() + " " + e.toString());
        }
        writeToLog = shouldLog;
        System.out.println("AppWorks SDK Event Bus Logging active=" + shouldLog);
    }


    public static void info(String message) {
        String msg = String.format(LOG_LINE, nowStamp(), message);
        if (writeToLog) {
            System.out.println(msg);
        }
    }

    public static void error(String message) {
        String msg = String.format(ERR_LOG_LINE, nowStamp(), message, "");
        if (writeToLog) {
            System.err.println(msg);
        }
    }

    public static void error(String errString, Throwable error) {
        String err = String.format(ERR_LOG_LINE, nowStamp(), error.getClass().getSimpleName(), errString);
        if (writeToLog) {
            System.err.println(err);
        }
    }

    private static String nowStamp() {
        return RFC_1123_DATE_TIME.format(now());
    }

}
