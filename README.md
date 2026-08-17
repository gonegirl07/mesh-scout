# MeshScout

**Guided Wi-Fi signal measurement & mesh node placement finder for Android**

MeshScout helps ordinary users (non-experts) find the best positions to place Wi-Fi Mesh nodes inside a home or small office.

Unlike classic Wi-Fi Analyzer apps that only list networks and signal strength, MeshScout walks you through a clear process:

1. Create a measurement session  
2. Mark the current Mesh Controller / Router location  
3. Mark important usage points (bedroom, living room, desk…)  
4. Enter **“Find Node Position”** mode — walk around while the app shows live RSSI from the Controller + a simple placement score  
5. Save history for later comparison  

**Languages:** English + Tiếng Việt (more languages welcome later)

---

## Current Status

- [x] GitHub repository created  
- [x] **Product Requirements Document (PRD) completed** → see [`PRD.md`](./PRD.md)  
- [x] Android Wi-Fi scanning limitations researched (Android 10 → 15/16)  
- [ ] Project scaffolding (Kotlin + Jetpack Compose)  
- [ ] Core MVP features  

**Tech stack (confirmed):** Kotlin · Jetpack Compose · Material 3 · Room · Coroutines/Flow  
**Package name:** `com.meshscout.app`  
**Min SDK:** 26 · **Target SDK:** 35/36  

---

## Quick Links

| Document | Description |
|----------|-------------|
| [PRD.md](./PRD.md) | Full Product Requirements Document (bilingual EN/VI) – goals, personas, user stories, functional requirements, scoring formula, technical constraints, recommended issues |
| [Issue #1](https://github.com/gonegirl07/mesh-scout/issues/1) | Research & implement reliable WiFi scanning |

---

## Goals (MVP)

- Help non-experts systematically measure and decide mesh node placement  
- Show live RSSI of the controller + simple placement score while walking  
- Privacy-first: no location data leaves the device, no accounts required  
- Full bilingual UI (English + Vietnamese)  
- Clean, documented, contribution-friendly open-source codebase  

---

## High-level Roadmap

### Phase 1 – Foundation (Now)
- [x] Repository + PRD  
- [ ] Android project scaffolding  
- [ ] Permission handling (FINE_LOCATION / NEARBY_WIFI_DEVICES)  
- [ ] Basic WifiScanner with throttling awareness  

### Phase 2 – Core Features (MVP)
- [ ] Session + Usage points + Candidate positions (Room)  
- [ ] Mark Controller / Mark Usage Points screens  
- [ ] Live “Find Node Position” mode + scoring engine  
- [ ] Session history  

### Phase 3 – Polish
- [ ] Better guidance & visual feedback  
- [ ] Export / language switcher  
- [ ] Dark theme + accessibility  

### Phase 4 – Community-driven
- [ ] Floor-plan / heatmap experiments  
- [ ] More languages  
- [ ] Advanced scoring models  

---

## Contributing

This project is in early stage. The PRD is the source of truth for scope.

Once scaffolding is done, contributions (code, translations, testing on real devices, ideas) are very welcome.

Please open an issue or discussion before large changes.

---

## License

To be decided (likely MIT or Apache 2.0)

---

**MeshScout – Measure. Walk. Place. Better Wi-Fi for everyone.**  
**Đo. Đi. Đặt. Wi-Fi tốt hơn cho mọi người.**
