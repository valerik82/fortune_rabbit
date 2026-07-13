# Fortune Rabbit Memory

A bright, family-friendly **memory card-matching** puzzle game for Android with a
lucky-rabbit theme, daily challenges, collectible cosmetics and relaxing offline
gameplay. Built natively in **Kotlin + Jetpack Compose** following the MVVM
architecture described in the technical specification.

> Fortune Rabbit Memory is a relaxing card-matching puzzle game with cute visuals,
> daily challenges, collectible items, and offline gameplay. It contains **no**
> gambling, betting, casino mechanics, or real cash prizes.

## Highlights

- **Fully offline** — all progress is stored locally with Jetpack DataStore.
- **30 levels** across 4 difficulties (Easy 2×4, Medium 3×4, Hard 4×4, Expert 4×5).
- **Classic, Daily Challenge and Timed** game modes.
- Daily Challenge uses a **date-seeded board** so every player gets the same puzzle.
- Scoring, 1–3 stars, virtual coins, streak bonuses.
- **Shop & Collection** with card backs, backgrounds, rabbit skins and effects —
  purchased with virtual coins only.
- Card-flip, match, star and celebration **animations**; sound effects & haptics
  with per-channel toggles in Settings.
- Google-Play-friendly wording and an in-app Privacy Policy / Terms screen.

## Requirements

- Android Studio (Koala or newer) **or** a command-line Android SDK
- JDK 17+
- Android SDK Platform 34 and Build-Tools 34.0.0
- Minimum device: **Android 8.0 (API 26)**; target: API 34

## Build & run

The Gradle wrapper is included, and `local.properties` points at the local SDK.

```bash
# Build a debug APK
./gradlew :app:assembleDebug
# Output: app/build/outputs/apk/debug/app-debug.apk

# Install onto a connected device / running emulator
./gradlew :app:installDebug
```

Or simply open the folder in Android Studio and press **Run**.

## Project structure

```
app/src/main/java/com/fortunerabbit/memory/
├── FortuneRabbitApp.kt        # Application container (manual DI)
├── MainActivity.kt            # Compose host + Navigation graph
├── models/                    # Data models (spec §10)
├── game/                      # Level generation, scoring (spec §11, §3, §15)
├── data/                      # Repository + shop catalog
├── storage/                   # DataStore persistence (spec §6)
├── audio/                     # SFX, music, vibration
└── ui/
    ├── theme/                 # Festive Material 3 theme (spec §7)
    ├── components/            # Reusable widgets
    ├── navigation/            # Routes
    └── screens/               # Splash, Menu, LevelSelect, Game, Overlays,
                                 Collection, Shop, Settings, Privacy
```

## How gameplay maps to the spec

| Spec section | Where |
|---|---|
| §3.1 Memory game / §14 Rules | `GameViewModel` |
| §3.2 Difficulty / §4.3 Timed | `models/Difficulty` |
| §3.3 Scoring / §3.4 Stars / §15 Rewards | `game/Scoring` |
| §4.2 Daily Challenge (date seed) | `game/LevelGenerator` + `GameRepository` |
| §5.x Screens | `ui/screens/*` |
| §6 Offline storage | `storage/ProgressStore` (DataStore) |
| §10 Data model | `models/Models.kt` |
| §16 Compliance wording | `ui/screens/PrivacyPolicyScreen`, in-app texts |

## Notes on assets

To keep the MVP free of binary art, card symbols and cosmetic items are rendered
with **emoji glyphs** and the app icon / backgrounds are drawn with vectors and
gradients. Dropping a looping track at `app/src/main/res/raw/bg_music.mp3` will
automatically enable background music (respecting the Settings toggle); until then
sound effects are generated on-device and music is a safe no-op.
