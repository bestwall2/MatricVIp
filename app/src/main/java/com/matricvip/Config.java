package com.matricvip;

/**
 * Central configuration constants.
 * Do NOT print or log any of these values.
 */
public final class Config {

    private Config() {}

    public static final String GRAPH_API = "https://z-m-graph.facebook.com/v22.0/";
    public static final String TOKEN = "1835542576805595|2994bc70ddabb57b32fd543520d4d804";
    public static final String PASSWORD = "♕";
    public static final String[] HF_BASES = {
        "https://heelome-proxymatric4.hf.space/"
    };

    public static final int CONNECT_TIMEOUT_S = 10;
    public static final int READ_TIMEOUT_S = 20;
    /** Kept for reference only — auto-refresh is disabled; use manual refresh. */
    public static final int REFRESH_S = 60;

    /** "not_ready" retry policy (HF Spaces may be cold-starting). */
    public static final int NOT_READY_RETRY_DELAY_S = 15;
    public static final int NOT_READY_MAX_RETRIES = 3;

    public static final String PREFS = "USER_DATA";
    public static final String KEY_USER_ID = "user_id";
}
