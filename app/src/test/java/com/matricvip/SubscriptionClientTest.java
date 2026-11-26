package com.matricvip;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Debug-only unit test: verifies gzip + base64 decode and AES/ECB/PKCS5Padding
 * URL decryption for userId = "test1234".
 */
public class SubscriptionClientTest {

    private static final String TEST_USER_ID = "test1234";
    private static final String SAMPLE_URL = "https://stream.example.com/live/ch1.m3u8";

    @Test
    public void decodeGzipBase64AndDecryptUrl1() throws Exception {
        byte[] key = MessageDigest.getInstance("SHA-256")
                .digest((Config.PASSWORD + TEST_USER_ID).getBytes(StandardCharsets.UTF_8));
        assertNotNull(key);
        assertEquals(32, key.length);

        Cipher encCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        encCipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
        String encUrl1 = Base64.getEncoder().encodeToString(
                encCipher.doFinal(SAMPLE_URL.getBytes(StandardCharsets.UTF_8)));

        String payloadJson = "[{\"img\":\"https://img.example.com/1.png\",\"name\":\"Ch1\",\"Url1\":\""
                + encUrl1 + "\"}]";

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (GZIPOutputStream gz = new GZIPOutputStream(bos)) {
            gz.write(payloadJson.getBytes(StandardCharsets.UTF_8));
        }
        String unpaddedBase64 = Base64.getEncoder().withoutPadding()
                .encodeToString(bos.toByteArray());

        // Pad base64, decode, gunzip, UTF-8
        int mod = unpaddedBase64.length() % 4;
        String padded = mod == 0 ? unpaddedBase64 : unpaddedBase64 + "====".substring(mod);
        byte[] decoded = Base64.getDecoder().decode(padded);

        byte[] outBytes;
        if (decoded.length >= 2 && (decoded[0] & 0xFF) == 0x1f && (decoded[1] & 0xFF) == 0x8b) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            try (GZIPInputStream gzIn = new GZIPInputStream(new ByteArrayInputStream(decoded))) {
                byte[] buf = new byte[4096];
                int n;
                while ((n = gzIn.read(buf)) != -1) {
                    out.write(buf, 0, n);
                }
            }
            outBytes = out.toByteArray();
        } else {
            outBytes = decoded;
        }
        String decodedJson = new String(outBytes, StandardCharsets.UTF_8);
        assertEquals(payloadJson, decodedJson);

        Cipher decCipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        decCipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
        String decryptedUrl = new String(
                decCipher.doFinal(Base64.getDecoder().decode(encUrl1)),
                StandardCharsets.UTF_8);
        assertEquals(SAMPLE_URL, decryptedUrl);
    }

    @Test
    public void newFormatCarriesUserAndChannels() throws Exception {
        String json = "{\"user\":{\"date\":\"2027/05/01\",\"phone\":\"0629996310\","
                + "\"name\":\"Ahmed\",\"android_id\":\"f7e517a67ad64575\"},"
                + "\"channels\":[{\"img\":\"i\",\"name\":\"Ch1\",\"Url1\":\"\"}]}";
        com.google.gson.JsonObject o = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
        assertEquals("Ahmed", o.getAsJsonObject("user").get("name").getAsString());
        assertEquals(1, o.getAsJsonArray("channels").size());
    }
}
