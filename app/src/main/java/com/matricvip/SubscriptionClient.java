package com.matricvip;

import android.util.Base64;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Fetches, decodes, and decrypts subscription-gated channels.
 * Uses a normal OkHttpClient with default TLS validation.
 */
public final class SubscriptionClient {

    private static final String TAG = "SubscriptionClient";

    private static final OkHttpClient HTTP = new OkHttpClient.Builder()
            .connectTimeout(Config.CONNECT_TIMEOUT_S, TimeUnit.SECONDS)
            .readTimeout(Config.READ_TIMEOUT_S, TimeUnit.SECONDS)
            .writeTimeout(Config.READ_TIMEOUT_S, TimeUnit.SECONDS)
            .build();

    public enum Status {
        OK,
        NOT_SUBSCRIBED,
        NOT_READY,
        NETWORK_ERROR,
        GRAPH_ERROR,
        BAD_PAYLOAD,
        DECRYPT_FAILURE
    }

    /** Subscriber details returned by the server after the android_id is verified. */
    public static final class UserInfo {
        public final String name;
        public final String phone;
        public final String date;      // subscription end, "yyyy/MM/dd"
        public final String androidId;

        public UserInfo(String name, String phone, String date, String androidId) {
            this.name = name == null ? "" : name.trim();
            this.phone = phone == null ? "" : phone.trim();
            this.date = date == null ? "" : date.trim();
            this.androidId = androidId == null ? "" : androidId.trim();
        }
    }

    public static final class Result {
        public final Status status;
        public final List<Channel> channels;
        public final UserInfo user;
        public final String arabicMessage;

        private Result(Status status, List<Channel> channels, UserInfo user, String arabicMessage) {
            this.status = status;
            this.channels = channels;
            this.user = user;
            this.arabicMessage = arabicMessage;
        }

        public static Result ok(List<Channel> channels, UserInfo user) {
            return new Result(Status.OK, channels, user, null);
        }

        public static Result notSubscribed() {
            return new Result(Status.NOT_SUBSCRIBED, null, null, "الاشتراك مطلوب");
        }

        public static Result notReady() {
            return new Result(Status.NOT_READY, null, null, "الخدمة غير جاهزة حالياً، أعد المحاولة بعد قليل");
        }

        public static Result error(Status status, String arabicMessage) {
            return new Result(status, null, null, arabicMessage);
        }
    }

    private SubscriptionClient() {}

    public static Result fetch(String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            return Result.notSubscribed();
        }
        // The server trims + lower-cases the android_id before deriving the key.
        userId = userId.trim().toLowerCase(java.util.Locale.ROOT);

        Result notReady = null;

        for (int retry = 0; retry <= Config.NOT_READY_MAX_RETRIES; retry++) {
            List<String> bases = new ArrayList<>(Arrays.asList(Config.HF_BASES));
            Collections.shuffle(bases);

            Result lastError = Result.error(
                    Status.NETWORK_ERROR, "تعذر الاتصال بالشبكة، تحقق من الإنترنت");
            boolean sawNotReady = false;

            for (String hfBase : bases) {
                Result res = fetchFromBase(hfBase, userId);
                if (res.status == Status.OK || res.status == Status.NOT_SUBSCRIBED) {
                    return res;
                }
                if (res.status == Status.NOT_READY) {
                    sawNotReady = true;
                    continue;
                }
                lastError = res;
            }

            if (!sawNotReady) {
                return lastError;
            }

            notReady = Result.notReady();
            if (retry == Config.NOT_READY_MAX_RETRIES) {
                break;
            }

            try {
                Thread.sleep(Config.NOT_READY_RETRY_DELAY_S * 1000L);
            } catch (InterruptedException interrupted) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        return notReady;
    }

    private static Result fetchFromBase(String hfBase, String userId) {
        String rawJson;
        try {
            long ts = System.currentTimeMillis();
            String encodedUserId = URLEncoder.encode(userId, StandardCharsets.UTF_8.name());
            String targetUrl = hfBase + "?_=" + ts + "&id=" + encodedUserId;

            FormBody body = new FormBody.Builder(StandardCharsets.UTF_8)
                    .add("id", targetUrl)
                    .add("scrape", "true")
                    .add("suppress_http_code", "1")
                    .add("access_token", Config.TOKEN)
                    .build();

            Request request = new Request.Builder()
                    .url(Config.GRAPH_API)
                    .post(body)
                    .build();

            try (Response response = HTTP.newCall(request).execute()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Graph HTTP " + response.code());
                }
                if (response.body() == null) {
                    return Result.error(Status.NETWORK_ERROR, "تعذر الاتصال بالشبكة: استجابة فارغة");
                }
                rawJson = response.body().string();
            }
        } catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Network error: " + e.getClass().getSimpleName());
            }
            return Result.error(Status.NETWORK_ERROR, "تعذر الاتصال بالشبكة، تحقق من الإنترنت");
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Request build error: " + e.getClass().getSimpleName());
            }
            return Result.error(Status.NETWORK_ERROR, "تعذر إرسال الطلب");
        }

        return parseAndDecrypt(rawJson, userId);
    }

    static Result parseAndDecrypt(String rawJson, String userId) {
        userId = userId == null ? "" : userId.trim().toLowerCase(java.util.Locale.ROOT);
        try {
            JsonElement rootEl = JsonParser.parseString(rawJson);
            if (!rootEl.isJsonObject()) {
                return Result.error(Status.BAD_PAYLOAD, "بيانات غير صالحة من الخادم");
            }
            JsonObject outer = rootEl.getAsJsonObject();

            if (outer.has("error") && !outer.get("error").isJsonNull()) {
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Graph error envelope received");
                }
                return Result.error(Status.GRAPH_ERROR, "خطأ من الخادم، أعد المحاولة لاحقاً");
            }

            String b64 = null;
            if (outer.has("site_name") && !outer.get("site_name").isJsonNull()) {
                b64 = outer.get("site_name").getAsString();
            } else if (outer.has("title") && !outer.get("title").isJsonNull()) {
                b64 = outer.get("title").getAsString();
            }

            if (b64 == null || b64.trim().isEmpty()) {
                return Result.error(Status.BAD_PAYLOAD, "استجابة ناقصة من الخادم");
            }

            String innerJson = decodePayload(b64.trim());
            if (innerJson == null) {
                return Result.error(Status.BAD_PAYLOAD, "تعذر فك ترميز بيانات الخادم");
            }

            JsonElement innerEl = JsonParser.parseString(innerJson);
            UserInfo userInfo = null;
            JsonArray arr = null;

            if (innerEl.isJsonObject()) {
                JsonObject obj = innerEl.getAsJsonObject();

                // Server error envelope: {"ok":0,"e":"not_subscribed"}
                if (obj.has("ok") && obj.get("ok").getAsInt() == 0 && obj.has("e")) {
                    String errCode = obj.get("e").getAsString();
                    if ("not_subscribed".equals(errCode)) {
                        return Result.notSubscribed();
                    }
                    if ("not_ready".equals(errCode)) {
                        return Result.notReady();
                    }
                    return Result.error(Status.BAD_PAYLOAD, "استجابة غير متوقعة من الخادم");
                }

                // New success format: {"user":{...},"channels":[...]}
                if (obj.has("channels") && obj.get("channels").isJsonArray()) {
                    arr = obj.getAsJsonArray("channels");
                }
                if (obj.has("user") && obj.get("user").isJsonObject()) {
                    JsonObject u = obj.getAsJsonObject("user");
                    userInfo = new UserInfo(
                            strOrEmpty(u, "name"),
                            strOrEmpty(u, "phone"),
                            strOrEmpty(u, "date"),
                            strOrEmpty(u, "android_id"));
                }
            } else if (innerEl.isJsonArray()) {
                arr = innerEl.getAsJsonArray(); // legacy server: bare channel array
            }

            if (arr == null) {
                return Result.error(Status.BAD_PAYLOAD, "تنسيق القنوات غير صالح");
            }

            byte[] aesKey = deriveKey(Config.PASSWORD, userId);
            if (aesKey == null) {
                return Result.error(Status.DECRYPT_FAILURE, "فشل تهيئة مفتاح التشفير");
            }

            List<Channel> channels = new ArrayList<>();
            int encryptedCount = 0;
            int decryptFailCount = 0;

            for (JsonElement item : arr) {
                if (!item.isJsonObject()) continue;
                JsonObject ch = item.getAsJsonObject();
                String img = strOrEmpty(ch, "img");
                String name = strOrEmpty(ch, "name");
                String encUrl = strOrEmpty(ch, "Url1");

                if (encUrl.isEmpty()) {
                    channels.add(new Channel(img, name, ""));
                    continue;
                }

                encryptedCount++;
                String plainUrl = decryptUrl(encUrl, aesKey);
                if (plainUrl == null || plainUrl.isEmpty()) {
                    decryptFailCount++;
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Channel decrypt failed");
                    }
                    continue;
                }
                channels.add(new Channel(img, name, plainUrl));
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "decrypt stats: encrypted=" + encryptedCount
                        + ", failed=" + decryptFailCount);
            }

            // A wrong key / wrong user id is a decryption problem, not an
            // explicit "not_subscribed" from the server. Show a real error
            // instead of blocking a valid user with the subscription screen.
            if (encryptedCount > 0 && decryptFailCount == encryptedCount) {
                return Result.error(Status.DECRYPT_FAILURE,
                        "فشل فك تشفير القنوات، تحقق من معرف المستخدم");
            }

            if (channels.isEmpty() && decryptFailCount > 0) {
                return Result.error(Status.DECRYPT_FAILURE,
                        "فشل فك تشفير القنوات، تحقق من معرف المستخدم");
            }

            return Result.ok(channels, userInfo);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Payload parse error: " + e.getClass().getSimpleName());
            }
            return Result.error(Status.BAD_PAYLOAD, "بيانات غير صالحة من الخادم");
        }
    }

    static String decodePayload(String b64) {
        try {
            String normalized = b64.replace("\n", "").replace("\r", "").trim();
            int mod = normalized.length() % 4;
            if (mod != 0) {
                StringBuilder sb = new StringBuilder(normalized);
                for (int i = 0; i < 4 - mod; i++) {
                    sb.append('=');
                }
                normalized = sb.toString();
            }
            byte[] bytes = Base64.decode(normalized, Base64.DEFAULT);
            if (bytes.length >= 2 && (bytes[0] & 0xFF) == 0x1f && (bytes[1] & 0xFF) == 0x8b) {
                bytes = gunzip(bytes);
                if (bytes == null) return null;
            }
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private static byte[] gunzip(byte[] compressed) {
        try (GZIPInputStream gz = new GZIPInputStream(new ByteArrayInputStream(compressed));
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            byte[] buf = new byte[4096];
            int n;
            while ((n = gz.read(buf)) != -1) {
                out.write(buf, 0, n);
            }
            return out.toByteArray();
        } catch (IOException e) {
            return null;
        }
    }

    static byte[] deriveKey(String password, String userId) {
        try {
            String seed = password + userId;
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(seed.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    static String decryptUrl(String encB64, byte[] aesKey) {
        try {
            String normalized = encB64.replace("\n", "").replace("\r", "").trim();
            int mod = normalized.length() % 4;
            if (mod != 0) {
                StringBuilder sb = new StringBuilder(normalized);
                for (int i = 0; i < 4 - mod; i++) sb.append('=');
                normalized = sb.toString();
            }
            byte[] cipher = Base64.decode(normalized, Base64.DEFAULT);
            SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");
            Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] plain = c.doFinal(cipher);
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    private static String strOrEmpty(JsonObject obj, String key) {
        if (obj.has(key) && !obj.get(key).isJsonNull()) {
            return obj.get(key).getAsString();
        }
        return "";
    }
}
