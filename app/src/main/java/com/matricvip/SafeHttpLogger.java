package com.matricvip;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Safe HTTP request logger for Logcat.
 *
 * <p>Logs only: method, host, path, status code, duration, body size and a
 * masked Android ID. Never logs headers, query values, tokens, user IDs,
 * cipher text, decrypted URLs or response bodies.</p>
 */
public final class SafeHttpLogger implements Interceptor {

    private static final String TAG = "HTTP";
    private static volatile String deviceLabel = "";

    public SafeHttpLogger() {
    }

    /** Call once from {@link SketchApplication#onCreate()}. */
    public static void init(Context context) {
        try {
            String id = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.ANDROID_ID);
            deviceLabel = UserIdHelper.mask(
                    id == null ? "" : id.trim().toLowerCase(Locale.ROOT));
        } catch (Exception ignored) {
            deviceLabel = "";
        }
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();

        long startNanos = System.nanoTime();
        Response response;
        try {
            response = chain.proceed(request);
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "FAIL " + safeTarget(request) + " device=" + deviceLabel
                        + " cause=" + e.getClass().getSimpleName());
            }
            throw e;
        }

        if (BuildConfig.DEBUG) {
            long millis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNanos);
            long bodyBytes = request.body() == null ? 0 : request.body().contentLength();

            Log.d(TAG, request.method() + " " + safeTarget(request)
                    + " -> " + response.code()
                    + " in " + millis + "ms"
                    + " body=" + bodyBytes + "B"
                    + " device=" + deviceLabel);
        }
        return response;
    }

    /** scheme://host/encodedPath only — query string is intentionally omitted. */
    private static String safeTarget(Request request) {
        String scheme = request.url().scheme();
        String host = request.url().host();
        String path = request.url().encodedPath();
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        return scheme + "://" + host + path;
    }
}
