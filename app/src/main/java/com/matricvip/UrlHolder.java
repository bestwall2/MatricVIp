package com.matricvip;

/**
 * In-process holder for passing a decrypted stream URL to PlayerActivity
 * without writing it to Intent extras or disk.
 */
public final class UrlHolder {

    private static volatile String sUrl;

    private UrlHolder() {}

    public static void set(String url) {
        sUrl = url;
    }

    public static String getAndClear() {
        String url = sUrl;
        sUrl = null;
        return url;
    }
}
