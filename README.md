# Arrow Escape

A tap-away arrow puzzle game for Android. The board is full of arrows; tap one to fling it off the board — but only if every cell in its path, all the way to the edge, is empty. Chain combos, dodge blocked arrows, and clear the whole board.

Four ways to play: **Classic** (a 120-level journey, 3 lives per level), **Blitz** (60 seconds, clear as many arrows as you can), **Zen** (endless and calm — no lives, no pressure), and **Daily Challenge** (one seeded board a day, keep your streak alive).

Special arrows spice things up: golden arrows worth 5×, bombs that blast their 8 neighbors (with chain reactions), and frozen arrows that need two taps.

## Tech stack

- Kotlin 2.0.20 + Jetpack Compose (composeBom 2024.09.00), Material 3
- Android Gradle Plugin 8.5.2, Gradle 8.9
- compileSdk 34 / minSdk 26 / targetSdk 34, Java 17
- Libraries: core-ktx 1.13.1, lifecycle-runtime-ktx 2.8.6, activity-compose 1.9.2, navigation-compose 2.7.7, datastore-preferences 1.1.1
- Persistence: DataStore preferences; synthesized sound effects; haptics on supported devices

## Build

CI builds the debug APK on every push, pull request, and manual dispatch (see `.github/workflows/android.yml`); the APK is uploaded as the `ArrowEscape-debug-apk` artifact.

Locally:

1. Install Gradle 8.9 (no wrapper in this repo).
2. Run `gradle :app:assembleDebug`.
3. The APK lands at `app/build/outputs/apk/debug/`.

## Project structure

```
settings.gradle.kts / gradle.properties / gradle/libs.versions.toml
app/
  build.gradle.kts
  proguard-rules.pro
  src/main/
    AndroidManifest.xml
    res/values/{colors,strings,themes}.xml
    res/drawable/ic_launcher_foreground.xml
    res/mipmap-anydpi-v26/{ic_launcher,ic_launcher_round}.xml
    java/com/bulktools/arrowescape/
      MainActivity.kt          # entry point, Compose nav host
      ui/                      # Compose screens: menu, game, results, stats, settings, howto
      engine/                  # pure-Kotlin game logic: board, rules, scoring, levels
      data/                    # DataStore persistence (stars, stats, settings, streaks)
      audio/                   # synthesized sound effects
      util/                    # haptics, helpers
```

See `GAME_DESIGN.md` for the full design: modes, scoring, level generation, and the solvability argument.
