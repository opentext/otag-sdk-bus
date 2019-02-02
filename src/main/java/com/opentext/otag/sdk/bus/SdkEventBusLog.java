package com.opentext.otag.sdk.bus;

import static java.time.ZonedDateTime.now;
import static java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME;

/**
 * stdout out based logger for the SDK event bus.
 */
public class SdkEventBusLog {

    // a String set top "true" enables file logging
    public static final String SDK_EVENT_LOGGING_ENV_VAR = "AWG_SDK_EVENT_BUS_LOGGING_ENABLED_ENV";

    private static final String LOG_LINE = "%s [%s-state=%s] - INFO - %s";
    private static final String ERR_LOG_LINE = "%s [%s-state=%s]- ERROR - %s %s";

    // access to the underlying environment
    private SystemEnvironment systemEnvironment;

    /**
     * Should this logger actually perform any logging?
     */
    private boolean writeToLog;

    private static SdkEventBusLog instance;

    SdkEventBusLog(SystemEnvironment systemEnvironment) {
        this.systemEnvironment = systemEnvironment;
    }

    static void setInstance(SdkEventBusLog instance) {
        SdkEventBusLog.instance = instance;
    }

    void initializeUsingEnv() {
        try {
            writeToLog = systemEnvironment.getenvBool(SDK_EVENT_LOGGING_ENV_VAR);
        } catch (Exception e) {
            System.err.println("Failed to process SDK Event Bus Logging env var - " +
                    e.getMessage() + " " + e.toString());
        }

        System.out.println("AppWorks SDK Event Bus Logging active=" + writeToLog);
    }

    public static void info(String message) {
        String msg = String.format(LOG_LINE, nowStamp(), threadName(), threadState(), message);
        if (canWriteToLog()) {
            System.out.println(msg);
        }
    }

    public static void error(String message) {
        String msg = String.format(ERR_LOG_LINE, nowStamp(), threadName(), threadState(), message, "");
        if (canWriteToLog()) {
            System.err.println(msg);
        }
    }

    public static void error(String errString, Throwable error) {
        String err = String.format(ERR_LOG_LINE, nowStamp(), threadName(), threadState(),
                error.getClass().getSimpleName(), errString);
        if (canWriteToLog()) {
            System.err.println(err);
            error.printStackTrace();
        }
    }

    private static boolean canWriteToLog() {
        if (instance == null) {
            getInstance();
        }

        return instance != null && instance.isWriteToLog();
    }

    private static SdkEventBusLog getInstance() {
        if (instance == null) {
            instance = new SdkEventBusLog(new SystemEnvironment());
            instance.initializeUsingEnv();
        }

        return instance;
    }

    private static String nowStamp() {
        return RFC_1123_DATE_TIME.format(now());
    }

    private static String threadName() {
        return Thread.currentThread().getName();
    }

    private static String threadState() {
        return Thread.currentThread().getState().name();
    }

    public SystemEnvironment getSystemEnvironment() {
        return systemEnvironment;
    }

    public void setSystemEnvironment(SystemEnvironment systemEnvironment) {
        this.systemEnvironment = systemEnvironment;
    }

    public boolean isWriteToLog() {
        return writeToLog;
    }

    public void setWriteToLog(boolean writeToLog) {
        this.writeToLog = writeToLog;
    }
}
