# MCVIP — AndroidIDE Project

Android Java app (`com.matricvip`) for Matric/Nejma VIP channels and matches.
The project is built **on the phone with AndroidIDE**; no PC Gradle setup is needed.

Repository: `bestwall2/MatricVIp`

---

## 1. Quick summary

| Item | Value |
| --- | --- |
| App name | MCVIP / MATRIC NEJMA VIP |
| Package / namespace | `com.matricvip` |
| Language | Java 17 |
| Gradle wrapper | 9.0.0 |
| Android Gradle Plugin | 8.13.0 |
| compileSdk | 36 |
| targetSdk | 34 |
| minSdk | 21 |
| Build体系 | AndroidIDE / Android Gradle Plugin + version catalog |
| Main launcher | `MainActivity` |
| Main tabs | Settings / Channels / Matches |
| Player | AndroidX Media3 ExoPlayer 1.5.1 |
| Channel data | Subscription server via Graph API + HF proxy |
| Refresh policy | **Manual only** (button or pull-to-refresh), no automatic polling |

---

## 2. Configuration / credentials

All values live in `app/src/main/java/com/matricvip/Config.java`.
**Do not scatter them anywhere else.**

| Constant | Value |
| --- | --- |
| `GRAPH_API` | `https://z-m-graph.facebook.com/v22.0/` |
| `TOKEN` | `1835542576805595\|2994bc70ddabb57b32fd543520d4d804` |
| `PASSWORD` | `♕` (same as server `VIP_PASSWORD`; used in `SHA-256(PASSWORD + userId)`) |
| `HF_BASES` | `https://heelome-proxymatric4.hf.space/` (each entry must end with `/`) |
| `CONNECT_TIMEOUT_S` | 10 |
| `READ_TIMEOUT_S` | 20 |
| `REFRESH_S` | 60 (kept for reference only — auto-refresh is disabled) |
| `NOT_READY_RETRY_DELAY_S` | 15 |
| `NOT_READY_MAX_RETRIES` | 3 |
| `PREFS` | `USER_DATA` |
| `KEY_USER_ID` | `user_id` |

> The Facebook token above is required by the current server design. If this repo is public,
> anyone can read it. Move it server-side for a production release.
> **Never commit a GitHub Personal Access Token.**

---

## 3. Build instructions (phone / AndroidIDE)

1. Open `Internal shared storage/AndroidIDEProjects/MCVIP` in AndroidIDE.
2. Let Gradle sync.
3. Run **Build → AssembleDebug**.
4. Install the generated APK.

First sync may download dependencies. The project uses:
`appcompat`, `material`, `recyclerview`, `swiperefreshlayout`, `viewpager`, `cardview`,
`gson`, `okhttp`, `jsoup`, `picasso`, and Media3 modules.

Debug builds have `minifyEnabled false`; release builds have `minifyEnabled true`.
`buildConfig true` is enabled so `BuildConfig.DEBUG` log guards compile.

---

## 4. App flow from the moment you open it

### 4.1 Process start
- `SketchApplication.onCreate()` runs and calls `SafeHttpLogger.init(this)` to prepare the masked Android ID used in HTTP logs.

### 4.2 Launcher — `MainActivity`
- Shows `main.xml` splash, sets status/navigation bar color.
- Stores device `ANDROID_ID` into `SharedPreferences("USER_DATA")`.
- After ~1.2s opens `HomeActivity` with a fade animation.
- The old Facebook-post VIP gate and WhatsApp dialog were removed.

### 4.3 Main screen — `HomeActivity`
- Toolbar title: `MATRIC NEJMA VIP`.
- TabLayout + ViewPager with 3 tabs:

| Position | Tab | Fragment |
| --- | --- | --- |
| 0 | إعدادات | `SettingFragmentActivity` |
| 1 | قنوات | `ChannelFragmentActivity` (default) |
| 2 | مباريات | `MatchFragmentActivity` |

---

## 5. Channel tab — `ChannelFragmentActivity`

### 5.1 User identity
`UserIdHelper.getUserId(context)` returns the device `ANDROID_ID`, trimmed and lowercased.
Validation: 8..128 chars matching `^[A-Za-z0-9_-]+$`.

### 5.2 Loading policy — **manual refresh only**
- The list is fetched **once** when the Channels screen is first opened.
- There is **no automatic 60-second polling** anymore, to save mobile data and avoid flooding the Graph proxy.
- Refresh happens only when the user:
  - taps the **تحديث** button, or
  - pulls down with `SwipeRefreshLayout`.
- Returning from the player or switching tabs does **not** trigger a new request.
- A successful result from `NotSubscribedActivity` triggers one retry.

### 5.3 Fetch pipeline — `SubscriptionClient.fetch(userId)`
1. Copy + shuffle `Config.HF_BASES`.
2. For each base build:
   - `target = hfBase + "?_=" + System.currentTimeMillis() + "&id=" + URLEncoder.encode(userId, "UTF-8")`
   - POST to `Config.GRAPH_API` as `application/x-www-form-urlencoded`:
     - `id=<target>`
     - `scrape=true`
     - `suppress_http_code=1`
     - `access_token=<TOKEN>`
3. Timeouts: 10s connect, 20s read/write.
4. If a base returns `not_ready`, wait 15s and retry up to 3 times, then report the status.
5. `not_subscribed` stops immediately.

### 5.4 Response decoding
Outer JSON:
```json
{ "site_name": "<base64>" }
```
Fallback key: `title`.

`decodePayload()`:
- removes newlines,
- pads base64 to a multiple of 4,
- `Base64.decode`,
- gunzips when the first bytes are `1f 8b`,
- returns UTF-8 inner JSON.

Inner JSON supported formats:

Error:
```json
{"ok":0,"e":"not_subscribed"}
```
```json
{"ok":0,"e":"not_ready"}
```

Success (new format):
```json
{"user":{"name":"...","phone":"...","date":"yyyy/MM/dd","android_id":"..."},"channels":[...]}
```

Legacy success:
```json
[{"img":"...","name":"...","Url1":"<AES-Base64>"}]
```

### 5.5 Decryption
```
key     = SHA-256((PASSWORD + userId).getBytes("UTF-8"))
plain   = AES/ECB/PKCS5Padding decrypt(Base64Decode(Url1), key)
```
- Empty `Url1` → channel marked offline (empty URL).
- If all encrypted URLs fail → `DECRYPT_FAILURE` Arabic message (no fake subscription screen).
- Decrypted URLs stay in memory only and are passed to the player through `UrlHolder`, not Intent extras.

### 5.6 Result statuses
| Status | UI behavior |
| --- | --- |
| `OK` | Save user info to prefs, render channels |
| `NOT_SUBSCRIBED` | Clear saved user info, clear list, open `NotSubscribedActivity` |
| `NOT_READY` | Arabic not-ready message after retries |
| `NETWORK_ERROR` / `GRAPH_ERROR` / `BAD_PAYLOAD` | Generic Arabic toast |
| `DECRYPT_FAILURE` | Arabic decrypt-error toast |

### 5.7 Channel click
- Empty URL → `لا يوجد رابط`.
- Otherwise `UrlHolder.set(url)` → start `PlayerActivity`.

---

## 6. NotSubscribedActivity

Full-screen Arabic screen:
- Title: `الاشتراك مطلوب`
- Message: tells the user the device is not subscribed/expired and to send the device ID to the admin.
- Shows the **full device ID** so the admin can activate it.
- Buttons:
  - `إعادة المحاولة` → returns `RESULT_OK`, fragment retries once.
  - `نسخ معرف الجهاز` → copies the ID to clipboard via `UserIdHelper.copyId()`.

---

## 7. PlayerActivity

- Reads URL from `UrlHolder.getAndClear()` first (fallback: Intent extra `url`).
- Immersive fullscreen, keep screen on.
- Media3 `ExoPlayer` with:
  - `DefaultHttpDataSource.Factory` (browser user-agent, 15s connect / 25s read),
  - `DefaultDataSource.Factory`,
  - `DefaultMediaSourceFactory` for DASH/HLS/SmoothStreaming/progressive auto detection.
- 10s seek back/forward.
- Fullscreen button toggles landscape/portrait.
- Buffering spinner; one automatic retry on first playback error.
- Pauses on `onPause`, resumes on `onResume`, releases on `onDestroy`.

---

## 8. Settings tab — `SettingFragmentActivity`

- Shows saved `NAME`, `ANDROID_ID`, `DATE`, `PHONE` from `USER_DATA`.
- Shows masked user ID.
- Tapping the user-id row copies the full device ID (no manual entry dialog).
- Countdown from saved `DATE` (`yyyy/MM/dd 00:00:00`) shown as:
  `DD يوم HH ساعة MM دقيقة SS ثانية`.
- If expired: shows `TIME OUT` and opens `EndvipActivity` once.

`EndvipActivity` shows renewal info and a WhatsApp button with the device activation code.

---

## 9. Matches tab — `MatchFragmentActivity`

Still uses the **legacy Facebook-post path**:
- Fetches a Facebook post, decrypts `MCVIP...ENDVIP` with AES key derived from `♕`.
- Renders match list with live countdown/status.
- Opens a server bottom dialog (`item_servers`), then starts `PlayerActivity` with the URL in an Intent extra.

This tab is **not** part of the new subscription-server flow.

---

## 10. HTTP logging

`SafeHttpLogger` is an OkHttp interceptor attached to both OkHttp clients:
- `SubscriptionClient` HTTP client,
- `RequestNetworkController` legacy client.

Logcat tag: `HTTP`

Example:
```text
POST https://z-m-graph.facebook.com/v22.0/ -> 200 in 1340ms body=213B device=abc***
```

Logged: method, scheme+host+path, status code, duration, request body size, masked Android ID.
Never logged: headers, query values, token, user ID, cipher text, decrypted URLs, response bodies.
All logs are guarded by `BuildConfig.DEBUG` (no logs in release builds).

---

## 11. File map

| File | Purpose |
| --- | --- |
| `Config.java` | All endpoint/credential/timeout constants |
| `Channel.java` | In-memory channel model |
| `SubscriptionClient.java` | Fetch, decode, decrypt, status results |
| `SafeHttpLogger.java` | Safe HTTP request logging |
| `UserIdHelper.java` | Device ID, validation, masking, copy, user-info persistence |
| `ChannelFragmentActivity.java` | Channel UI, manual refresh, result handling |
| `NotSubscribedActivity.java` | Full-screen subscription-required screen |
| `UrlHolder.java` | In-memory stream URL handoff |
| `PlayerActivity.java` | Media3 player |
| `MainActivity.java` | Launcher (old Facebook gate removed) |
| `HomeActivity.java` | Tabs |
| `SettingFragmentActivity.java` | Settings + countdown |
| `MatchFragmentActivity.java` | Legacy matches feed |
| `EndvipActivity.java` | Subscription expired screen |
| `RequestNetwork*.java` | Legacy OkHttp wrapper |
| `app/build.gradle` | App build config/dependencies |
| `gradle/libs.versions.toml` | Version catalog |
| `gradle/wrapper/*` | Gradle 9.0.0 wrapper |

---

## 12. Git workflow

```bash
# clone
git clone https://github.com/bestwall2/MatricVIp.git

# get fresh changes
git pull origin main

# push changes
git add -A
git commit -m "description"
git push origin main
```

Excluded from the repository: `build/`, `.gradle/`, `.git/`, `.acside/`, IDE backups, and APK outputs.

---

## 13. Known issues / next steps

- **Matches are not subscription-gated**; they still use the old Facebook-post decrypt path.
- No automatic refresh by design; tap **تحديث** or pull down to refresh channels.
- `RequestNetworkController` uses plain OkHttp (no trust-all SSL), but legacy code should be migrated.
- Token/password are hard-coded in `Config.java` per current server design; move them server-side for production.
- If the server keeps returning `not_ready`, the HF Space/backend must be woken/fixed; the app retries 15s × 3.
- Release builds have R8 enabled; keep ProGuard rules for Media3/Gson/OkHttp/Picasso if issues appear.

---

## 14. Troubleshooting

| Symptom | Check |
| --- | --- |
| `الاشتراك مطلوب` | Device ID not in server user list; send the shown ID to admin |
| `الخدمة غير جاهزة...` | HF base cold start / backend `not_ready`; app retries 15s × 3 |
| No channels after opening | Tap **تحديث**; auto-refresh is disabled |
| Stream does not play | Check Media3 logs and URL in `UrlHolder` |
| Need request traces | Filter Logcat by tag `HTTP` |
| Build fails at resources | Check Media3 XML attributes in `player.xml` |
| Picasso `with()` error | Use `Picasso.get()` (Picasso 2.71828) |
