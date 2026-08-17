# MeshScout

**Android app to help ordinary users find the best positions for WiFi Mesh nodes in their home.**

MeshScout is a guided signal measurement tool (inspired by WiFi Analyzer) that helps you place mesh nodes optimally — even if you are not a networking expert.

- Measure signal strength from the main Mesh Controller / Router
- Mark important usage points (bedroom, living room, office...)
- Walk around and discover positions that have good backhaul signal **and** can cover your usage points well
- Simple scoring system to suggest good placement spots

**Languages:** English + Tiếng Việt

---

## How to open the project

1. Clone the repository:
   ```bash
   git clone https://github.com/gonegirl07/mesh-scout.git
   cd mesh-scout
   ```

2. Open **Android Studio** (latest stable recommended).

3. Select **File → Open** and choose the root folder of the project.

4. Wait for Gradle sync to finish (first time may take a few minutes).  
   Android Studio will automatically download the Gradle wrapper if needed.

5. Connect a **real Android device** (API 26+) — emulator is **not reliable** for WiFi RSSI measurement.

6. Run the `app` configuration.

---

## Tech Stack

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35
- **Local storage:** DataStore (Room later if needed)
- **Package:** `com.meshscout.app`

---

## Project Structure (high level)

```
app/src/main/java/com/meshscout/app/
├── MeshScoutApplication.kt
├── MainActivity.kt
├── ui/
│   ├── theme/
│   ├── navigation/
│   ├── components/
│   └── screens/
├── data/
│   ├── wifi/
│   ├── local/
│   └── repository/
├── domain/
└── util/
```

---

## Roadmap

### Phase 1 – Foundation (Current)
- [x] Create GitHub repository
- [x] Basic project structure + permissions
- [ ] Permission handling + WiFi Scanner
- [ ] Real-time RSSI display

### Phase 2 – Core Features (MVP)
- [ ] Create / manage measurement points
- [ ] Save measurement sessions
- [ ] "Find Node Position" mode with simple scoring
- [ ] Full English + Vietnamese UI

### Phase 3 – Usability & Polish
- [ ] Better guidance while walking
- [ ] Session history
- [ ] Improve scoring algorithm
- [ ] Dark mode polish

---

## License

Apache License 2.0

---

**Status:** Project skeleton ready. Start coding features.
