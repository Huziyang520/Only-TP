package com.onlytp.onlytpmod;

public final class AvalonLink {

    private static final String MARKER = "com.avalon.base.Constants";

    private static final boolean AVALON_PRESENT = isPresent();

    private AvalonLink() {
    }

    private static boolean isPresent() {
        try {
            Class.forName(MARKER);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static boolean isAvalonLoaded() {
        return AVALON_PRESENT;
    }
}
