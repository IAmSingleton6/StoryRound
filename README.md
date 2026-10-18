# Story Round

A real-time multiplayer story-writing game for Android, iOS, desktop and web, built with Kotlin Multiplatform and Compose Multiplatform.

Players take turns adding to one continuous story against a shrinking timer. When the last round ends, the finished story is saved on each player's device.

## How it plays

- One player creates a room and shares the four-character code. Up to 10 players can join.
- The host picks the number of rounds, who writes the opening line, and whether turns are limited to one word.
- After the opening line, each player gets one timed turn. The first turn is 15 seconds, and each turn after it is a second shorter, down to 5 seconds.
- Each player's contributions are highlighted in their colour. Finished stories appear in Story History.

## Project structure

| Module | Contents |
| --- | --- |
| `core` | Protocol messages, game rules and configuration shared by the client and server |
| `server` | Ktor server that runs every game over a WebSocket at `/game` |
| `app/shared` | Compose Multiplatform client: screens, view models, networking and local storage |
| `app/androidApp`, `app/desktopApp`, `app/webApp`, `app/iosApp` | Platform entry points |
| `testing` | Shared test helpers |

The server owns all game state, turn timers and validation. Clients only send actions and render the state they receive. The server keeps no database; finished stories are stored locally on each device.

## Requirements

- JDK 17 or newer
- Android Studio (Android) or Xcode (iOS), only for those platforms

## Running locally

Start the server:

```bash
./gradlew :server:run
```

Then start one or more clients:

```bash
./gradlew :app:desktopApp:run
./gradlew :app:webApp:wasmJsBrowserDevelopmentRun
./gradlew :app:androidApp:installDebug
```

For iOS, open `app/iosApp` in Xcode and run it on a simulator.

To try multiplayer, open two clients (two browser tabs work), create a room in one and join it from the other.

### Server address

Clients use the addresses in `config/development.properties`. The Android emulator uses `10.0.2.2` to reach your machine.

To override them without touching tracked files, create `config/local.properties` with the same keys, for example to test on a physical phone:

```properties
android.serverUrl=ws://192.168.1.20:8080/game
```

iOS reads its address from `app/iosApp/Configuration/Config.xcconfig`. An ignored `Local.xcconfig` next to it overrides it.

## Testing

```bash
./gradlew qualityCheck
./gradlew :core:jvmTest :testing:jvmTest :app:shared:jvmTest :server:test
```

`qualityCheck` runs ktlint and detekt. The test run covers unit tests, screen tests, server integration tests and full-stack tests that run the app against an in-memory server.

## Building for release

Set `STORY_ROUND_SERVER_URL` to point the clients at your server:

```bash
export STORY_ROUND_SERVER_URL=wss://game.example.com/game

./gradlew :app:webApp:wasmJsBrowserProductionWebpack
./gradlew :app:androidApp:assembleRelease -PversionName=1.0.0 -PversionCode=1
./gradlew :app:desktopApp:packageReleaseDistributionForCurrentOS -PversionName=1.0.0
./gradlew :server:buildFatJar
```

Android release builds are signed when `ANDROID_KEYSTORE_FILE`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS` and `ANDROID_KEY_PASSWORD` are set. Without them, the APK is unsigned.

The server is configured with environment variables:

| Variable | Default |
| --- | --- |
| `PORT` | `8080` |
| `HOST` | `0.0.0.0` |
| `LOG_LEVEL` | `info` (`debug`, `info`, `warn`, `error`) |

## CI

GitHub Actions runs the quality checks, tests and platform builds on every pull request and push to `main`. Pushing a `v*` tag runs the release workflow, which deploys the web app to GitHub Pages, publishes the server image to GHCR and attaches the Android and desktop installers to a GitHub release.
