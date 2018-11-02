package com.opentext.otag.sdk.bus;

public class SystemEnvironment {

    public String getenv(String name) {
        try {
            return removeFormattingChars(System.getenv(name));
        } catch (NullPointerException | SecurityException e) {
            System.err.println("Failed to resolve environment variable for name: " + name +
                    ", error=" + e.getClass() + " : " + e.getMessage());
            e.printStackTrace();
            return "";
        }
    }

    public static String removeFormattingChars(String input) {
       return input != null ? input.replaceAll("(\\r|\\n)", "") : "";
    }

}
