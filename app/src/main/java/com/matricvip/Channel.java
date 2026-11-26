package com.matricvip;

/**
 * In-memory channel model. Decrypted URLs are never persisted to disk.
 */
public final class Channel {

    public final String img;
    public final String name;
    public final String url;

    public Channel(String img, String name, String url) {
        this.img = img == null ? "" : img;
        this.name = name == null ? "" : name;
        this.url = url == null ? "" : url;
    }
}
