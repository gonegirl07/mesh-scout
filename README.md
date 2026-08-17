# MeshScout

**Android app to help ordinary users find the best positions for WiFi Mesh nodes in their home.**

MeshScout is a guided signal measurement tool (inspired by WiFi Analyzer) that helps you place mesh nodes optimally — even if you are not a networking expert.

- Measure signal strength from the main Mesh Controller / Router
- Mark important usage points (bedroom, living room, office...)
- Walk around and discover positions that have good backhaul signal **and** can cover your usage points well
- Simple scoring system to suggest good placement spots

**Languages:** English + Tiếng Việt

**Repo:** https://github.com/gonegirl07/mesh-scout  
**PRD:** [PRD.md](./PRD.md)

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

5. Connect a **real Android device** (API 26+) — emulator is **not reliable** for WiFi RSSI measurement.

6. Run the `app` configuration.

---

## Tech Stack

- **Language:** Kotlin 2.1
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + clean-ish packages
- **Min SDK:** 26 (Android 8.0)
- **Target SDK:** 35
- **Local storage:** DataStore (Room can be added later)
- **Package:** `com.meshscout.app`
- **License:** Apache 2.0

---

## Project Structure

```
app/src/main/java/com/meshscout/app/
├── MeshScoutApplication.kt
├── MainActivity.kt
├── data/
│   ├── wifi/          # WifiScanner, throttling, scan mapping
│   ├── local/         # DataStore / Room
│   └── repository/
├── domain/
│   ├── model/         # Session, UsagePoint, CandidatePosition, Score
│   └── usecase/
├── ui/
│   ├── theme/
│   ├── navigation/
│   ├── permissions/   # Rationale + request flow
│   ├── session/       # List / create / detail
│   ├── measure/       # Mark Controller, Mark Points, Live Find mode
│   └── components/    # ScoreGauge, RssiBadge…
└── util/
```

---

## Current Status (2026-08-17)

### Done
- [x] Repository + Apache 2.0 license
- [x] Detailed bilingual PRD
- [x] Modern Gradle + Compose project skeleton
- [x] Correct Wi-Fi / Location permissions (with `maxSdkVersion` + `NEARBY_WIFI_DEVICES`)
- [x] Placeholder vector launcher icon
- [x] Full package structure according to PRD
- [x] High-priority GitHub Issues created

### Next (start coding here)
See open issues: https://github.com/gonegirl07/mesh-scout/issues

Recommended order:
1. Permission rationale & request flow
2. WifiScanner wrapper (after quick permission research on real device)
3. Domain models + local persistence
4. Scoring engine
5. Mark Controller → Mark Usage Points → Live Find screen

---

## Roadmap

### Phase 1 – Foundation ✅
- [x] Repo, PRD, skeleton, permissions, structure, issues

### Phase 2 – Core Features (MVP)
- [ ] Permission handling + WiFi Scanner
- [ ] Session model + persistence
- [ ] Scoring engine
- [ ] Mark Controller / Usage Points / Live Find screens
- [ ] Session history

### Phase 3 – Polish
- [ ] Better guidance & throttling UX
- [ ] Improved scoring
- [ ] More complete bilingual strings
- [ ] Dark theme polish

---

## Contributing

Early stage. The best way to help right now is:
1. Test permission / scanning behavior on different Android versions & manufacturers
2. Comment on open issues with findings
3. Improve Vietnamese / English wording

---

**MeshScout – Measure. Walk. Place. Better Wi-Fi for everyone.**  
**Đo. Đi. Đặt. Wi-Fi tốt hơn cho mọi người.**
