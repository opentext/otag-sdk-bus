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

    public boolean getenvBool(String name) {
        String value = getenv(name);
        if (value.length() == 6) { // e.g. "true", or 'true'
            // some use cases, e.g. some docker setups, require the value to be quoted
            value = value.substring(1, value.length() - 1);
        }
        return Boolean.TRUE.toString().equalsIgnoreCase(value);
    }


    public static String removeFormattingChars(String input) {
       return input != null ? input.replaceAll("(\\r|\\n)", "") : "";
    }

}
