# ROXIFY — Feel Every Beat

An offline-first Android music player built with **Kotlin + Jetpack Compose**.

ROXIFY scans the audio already on your device and plays it completely offline — your
library, favourites, playlists and history never leave the phone. It also ships with a
small, clearly-labelled **demo** catalogue so the discovery surfaces (moods, trending,
recommendations) are usable out of the box.

> ROXIFY ships with **no streaming credentials and no scraping** of third-party
> services. Real streaming is opt-in and requires plugging in a licensed catalogue
> (see [Online music](#online-music)).

---

## ✨ Features

- **Home** — mood / genre shelves (Romantic, Sad, Chill, Party, Workout, Lo-fi), trending and recommendations.
- **Search** — songs, albums, artists and playlists, with recent-search history.
- **Local music** — scans device audio through `MediaStore`, grouped by folder.
- **Library** — songs, albums, artists and folders, with sorting by Title, Artist, Album, Duration or Recently added.
- **Playlists** — create, rename, delete, add/remove songs.
- **Favourites** and **Recently played**.
- **Player** — queue, shuffle, repeat (off / one / all), crossfade and gapless playback.
- **Equalizer** — toggleable, with sleep timer.
- **Profile & Settings** — theme (System / Dark / Light), dynamic colour, audio quality, crossfade, gapless, default shuffle/repeat, show-or-hide demo content.
- **Background playback** — Media3 foreground service with notification and lock-screen controls, plus hardware/Bluetooth media buttons.
- **Onboarding** and a dedicated permissions flow.
- **Theme** — premium black & neon-purple look with glassmorphism.

## 🧭 Screens

Splash · Onboarding · Home · Search (+ results) · Library · Album · Artist · Player ·
Queue · Playlist detail · Favourites · Recently played · Local music · Profile ·
Settings · Equalizer · Permissions · About

---

## 🛠️ Tech stack

| Area | Used |
|---|---|
| Language / UI | Kotlin 2.0.21, Jetpack Compose (Material 3) |
| Build | Android Gradle Plugin 8.7.3, Gradle 8.11.1, JDK 17 |
| Playback | AndroidX Media3 (ExoPlayer / MediaSession) 1.5.1 |
| Persistence | Room 2.6.1, DataStore Preferences 1.1.1 |
| Navigation | Navigation Compose 2.8.5 |
| Images | Coil 2.7.0 |
| Serialization | kotlinx.serialization 1.7.3, Coroutines 1.9.0 |

- `minSdk = 24`, `targetSdk = 35`, `compileSdk = 35`
- `applicationId` / namespace: `com.roxify.music`

---

## 🎧 Demo content

The bundled tracks (`app/src/main/assets/demo/*.wav`) are short, original,
royalty-free tones generated specifically for ROXIFY. Every entry is tagged
`SongSource.DEMO` ("ROXIFY Studio · *mood*", album *Neon Sessions (Demo)*) and is
rendered with a visible **DEMO** badge. Nothing here pretends to be a real licensed
recording, and demo content can be hidden in Settings.

---

## 🌐 Online music

ROXIFY defines a single abstraction for streamed content:

```
data/remote/OnlineMusic.kt   →  OnlineMusicProvider  (trending / recommendations / search)
                                OnlineMusicRepository (facade the UI talks to)
```

The default implementation is `OfflineDemoProvider`, which reports
`isConfigured = false` and serves the local demo library while the UI shows an honest
"no licensed catalogue configured" banner.

To enable real streaming, implement `OnlineMusicProvider` for a licensed source
(e.g. Jamendo / Deezer / your own licensed CDN) and provide its **server-issued token at
build time** — never in source:

```bash
# -P ROXIFY_ONLINE_TOKEN=...   (shown here as it is usually written, without the space)
./gradlew assembleRelease -PROXIFY_ONLINE_TOKEN="<server-issued-token>"
```

The value is surfaced to the app as `BuildConfig.ROXIFY_ONLINE_TOKEN`. An empty token
means offline demo mode.

---

## 🚀 Build & run

**Requirements:** JDK 17, the Android SDK (API 35), and Gradle 8.11.1.

> This repository does **not** include the Gradle wrapper. Use an installed Gradle, or
> add a wrapper once with:
> `gradle wrapper --gradle-version 8.11.1`

Point Gradle at your SDK via `local.properties` (not committed):

```properties
sdk.dir=/path/to/Android/Sdk
```

Then:

```bash
gradle assembleDebug     # build a debug APK
gradle assembleRelease   # build a release APK (see Signing below)
gradle test              # run unit tests
gradle installDebug      # install onto a connected device / emulator
```

APKs are written to `app/build/outputs/apk/`.

---

## 🔐 Signing

Release signing uses `keystore.properties` plus `keystore/roxify-release.jks`. Both are
**git-ignored** and are not part of this repository — supply your own for a real
release. If `keystore.properties` is absent, release builds fall back to debug signing
so local builds still work.

`keystore.properties` format:

```properties
storeFile=keystore/your-release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Replace any sample/demo keystore and use strong passwords before publishing to a store.

---

## 📱 Permissions

| Permission | Why |
|---|---|
| `READ_MEDIA_AUDIO` (Android 13+) / `READ_EXTERNAL_STORAGE` (≤ 12) | Read music already on the device |
| `POST_NOTIFICATIONS`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PLAYBACK` | Media notification / lock-screen controls |
| `WAKE_LOCK` | Keep playback alive |
| `INTERNET` | Only used if a licensed online catalogue is configured |

---

## 🔒 Privacy

Offline-first by design. Your library, favourites, playlists, settings and playback
history are stored locally (Room + DataStore) and are not uploaded. No streaming data is
harvested or sold.

---

## 🗂️ Project structure

```
app/src/main/java/com/roxify/music/
├─ data/        Room entities/DAOs, demo catalogue, local MediaStore scan,
│               online provider, repositories, DataStore settings, JSON mapper
├─ domain/      LibraryIndex, domain models (Song, Album, Artist, Playlist, Mood…)
├─ playback/    PlaybackService (foreground), PlaybackManager, EqualizerController,
│               MediaItemMapper, PlayerUiState
├─ di/          AppContainer (manual DI)
└─ ui/          components, navigation, screens, theme, util
gradle/libs.versions.toml   # dependency version catalog
```

## 🧪 Tests

Unit tests live in `app/src/test/java/com/roxify/music/` and cover the demo catalogue,
formatters, library indexing, the online-music facade, player UI state and song JSON.

```bash
gradle test
```

---

## 📄 Credits & license

ROXIFY is a personal project. The bundled demo audio consists of original royalty-free
tones generated for the app. No license file is currently included — add one before
distributing.
