# MCVIP

Android Java app (`com.matricvip`) for Matric/Nejma VIP channels and matches.  
This workspace copy is prepared for **AndroidIDE on-device builds** (no PC Gradle setup required).

## Current status

- AndroidIDE/Gradle project structure is in place.
- Build migrated to AGP/Gradle catalog style.
- Player migrated from legacy ExoPlayer 2 to **AndroidX Media3**.
- Old channel fetch path (`MCVIP...ENDVIP` Facebook post scrape/decrypt) was removed from the channel flow.
- New subscription-gated channel fetch is implemented via Graph + HF base + per-user AES URL decryption.

## Build setup (AndroidIDE / phone)

| Item | Value |
|---|---|
| Gradle wrapper | 9.0.0 |
| Android Gradle Plugin | 8.13.0 |
| compileSdk | 36 |
| targetSdk | 34 |
| minSdk | 21 |
| Java | 17 |
| Namespace / applicationId | `com.matricvip` |

### How to build

1. Open `/Internal shared storage/AndroidIDEProjects/MCVIP` in AndroidIDE.
2. Let Gradle sync.
3. Run **Build > AssembleDebug** / **Build APK**.
4. Install the generated debug APK.

> First build may download missing dependencies. Do not run Gradle setup on PC unless intentionally desired; project is meant to build on phone.

## Project layout

```text
MCVIP/
  app/
    build.gradle
    src/main/
      AndroidManifest.xml
      java/com/matricvip/
        Config.java                    # endpoint/secret/constants
        Channel.java                   # channel model (in-memory)
        SubscriptionClient.java        # fetch/decode/decrypt/subscription result
        ChannelFragmentActivity.java   # channel UI, userId prompt, subscription screen
        MainActivity.java              # launcher; old Facebook VIP gate removed
        HomeActivity.java              # tabs: Settings / Channels / Matches
        PlayerActivity.java            # Media3 player
        MatchFragmentActivity.java     # matches feed
        SettingFragmentActivity.java   # settings/user info
        RequestNetwork*.java           # legacy OkHttp wrapper (still used by old screens)
      res/
        layout/
        drawable/
        font/
  gradle/
    libs.versions.toml
    wrapper/
  build.gradle
  settings.gradle
  gradle.properties
```

## What was changed

### 1) AndroidIDE conversion
- Added Gradle wrapper 9.0.0, `settings.gradle`, root `build.gradle`, version catalog.
- Updated `app/build.gradle` to AGP 8.13.0, Java 17, AndroidX deps.
- Added missing dependencies: Media3, RecyclerView, SwipeRefreshLayout, CardView, ViewPager, OkHttp, Gson, Jsoup, Picasso.
- Added `SketchApplication`.
- Cleaned manifest/resource issues (removed broken debug/startup entries, fixed invalid font/resource placement).

### 2) Player migration (Media3)
- `PlayerActivity` now uses `androidx.media3.exoplayer.ExoPlayer`.
- Auto stream detection via `DefaultMediaSourceFactory` + `DefaultDataSource`.
- Supports DASH/HLS/SmoothStreaming/progressive depending on source/Media3 modules.
- Fullscreen toggle, immersive mode, buffering indicator, simple retry on first error.
- Seek increment set in player builder: 10s back/forward.

### 3) Subscription-gated channels
Old path removed:
- No more `https://www.facebook.com/.../MCVIP...ENDVIP` scraping in channel fetch.
- No more old Facebook-post AES blob as channel source.

New path:
- `Config.java` contains all endpoint/credentials/timeouts.
- `SubscriptionClient.java`:
  - shuffles `HF_BASES`, tries each until valid result.
  - builds target: `hfBase + "?_=" + ts + "&id=" + URLEncoder(userId)`.
  - POSTs form body to `GRAPH_API`: `id=<target>&scrape=true&suppress_http_code=1&access_token=<TOKEN>`.
  - decodes `site_name` (fallback `title`) from response: Base64 pad->decode, gunzip if `1f 8b`, UTF-8 JSON.
  - handles results:
    - `{"ok":0,"e":"not_subscribed"}` => `NotSubscribed` (stop, clear in-memory list, no auto retry).
    - `{"ok":0,"e":"not_ready"}` => retry after 15s, up to 3 times, then `NotReady`.
    - JSON array => decrypt per-channel `Url1`, return `Channels`.
- `Url1` decryption:
  - `key = SHA-256((PASSWORD + userId).getBytes("UTF-8"))`
  - `AES/ECB/PKCS5Padding`, Base64 input.
  - empty Url1 => channel marked unavailable.
  - if all encrypted channels fail decryption => `NotSubscribed`.
- `ChannelFragmentActivity`:
  - prompts/stores `user_id` in `SharedPreferences(Config.PREFS)`.
  - validates: 8..128 chars, `[A-Za-z0-9_-]`.
  - refreshes every 60s while foreground/resumed.
  - shows subscription-required dialog on `NotSubscribed`.
  - keeps last good list in memory only; no decrypted URL disk/log persistence.

## Subscription contract (server)

Request:
```text
POST https://z-m-graph.facebook.com/v22.0/
Content-Type: application/x-www-form-urlencoded

id=<HF_BASE>?_=<ts>&id=<userId>&scrape=true&suppress_http_code=1&access_token=<TOKEN>
```

Response envelope:
```json
{ "site_name": "<base64>" }
```

Decoded payload:
```json
{"ok":0,"e":"not_subscribed"}
```
or
```json
{"ok":0,"e":"not_ready"}
```
or
```json
[
  { "img": "https://...", "name": "Channel", "Url1": "<AES-Base64>" }
]
```

Decrypt:
```text
key = SHA-256((PASSWORD + userId).getBytes("UTF-8"))
plainUrl = AES/ECB/PKCS5Padding decrypt(Base64Decode(Url1), key)
```

## Config

All in `app/src/main/java/com/matricvip/Config.java`:

- `GRAPH_API`
- `TOKEN`
- `PASSWORD`
- `HF_BASES[]`
- `CONNECT_TIMEOUT_S = 10`
- `READ_TIMEOUT_S = 20`
- `REFRESH_S = 60`
- `PREFS = "USER_DATA"`
- `KEY_USER_ID = "user_id"`

**Security note:** TOKEN/PASSWORD are in app code by current design request. Do not add server-only secrets. Do not log token/userId/cipher/decrypted URLs.

## Important files

| File | Purpose |
|---|---|
| `app/src/main/java/com/matricvip/Config.java` | Central config/constants |
| `app/src/main/java/com/matricvip/Channel.java` | Channel model |
| `app/src/main/java/com/matricvip/SubscriptionClient.java` | Subscription fetch/decode/decrypt |
| `app/src/main/java/com/matricvip/ChannelFragmentActivity.java` | Channel UI + userId prompt + subscription dialog |
| `app/src/main/java/com/matricvip/MainActivity.java` | Launcher; old VIP FB gate removed |
| `app/src/main/java/com/matricvip/PlayerActivity.java` | Media3 playback |
| `app/build.gradle` | App dependencies/build config |
| `gradle/libs.versions.toml` | Dependency versions |

## Known issues / cautions

- **Old MainActivity VIP gate removed**: launcher now goes to Home; channel access is gated in channel screen. If you still need old app-level VIP behavior, implement it through subscription server, not Facebook post scrape.
- **Matches/Settings still use legacy code paths**. They were not fully refactored in this subscription pass.
- **RequestNetworkController** still contains trust-all SSL/hostname verifier from legacy code. This is insecure; replace with normal TLS validation before production release.
- **Decrypted URLs are memory-only** by design in new channel flow; avoid adding caching/logging.
- **AndroidIDE first sync** can be slow; dependency downloads depend on what's cached on device.

## Next setup / recommended work

1. **Build & smoke test on phone**
   - AssembleDebug.
   - Enter valid `user_id`.
   - Confirm channels load, refresh every 60s foreground, and play in Media3.

2. **Subscription UX hardening**
   - Add a dedicated "Subscription required" screen/activity if you want full-screen UX instead of dialog.
   - Add manual "Change user ID" action in Settings.

3. **Security cleanup**
   - Remove trust-all cert code from `RequestNetworkController`.
   - Audit all `Log.*`, Toasts, exceptions for sensitive data.
   - Consider moving token handling server-side/proxy if possible.

4. **App modernization**
   - Migrate old screens to ViewBinding/clean adapters.
   - Replace deprecated FragmentStatePagerAdapter with ViewPager2.
   - Add proper error states, empty states, pull-to-refresh on matches.

5. **Player enhancements**
   - Quality/track selector UI.
   - Better error classification (401/403/404/network/drm).
   - Optional external player/share.

6. **Release prep**
   - Proguard/R8 rules for Media3/OkHttp/Gson/Picasso if minify enabled.
   - Versioning, signing config, privacy/policy review.

## Quick troubleshooting

- **Resource linking fails**: check XML attributes against Media3/AndroidX versions in `libs.versions.toml`.
- **Picasso `with()` missing**: use `Picasso.get()` (Picasso 2.71828).
- **Channels show "subscription required"**: verify `user_id`, HF base availability, server `PASSWORD+userId` key, and encrypted `Url1`.
- **Not ready**: client retries 3 times every 15s, then shows generic not-ready message.
- **Empty/invalid response**: client tries next HF base; ensure HF_BASES are reachable and return expected envelope.
