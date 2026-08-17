# MeshScout

**Android app to help ordinary users find the best positions for WiFi Mesh nodes in their home.**

MeshScout is a guided signal measurement tool (inspired by WiFi Analyzer) that helps you place mesh nodes optimally — even if you are not a networking expert.

- Measure signal strength from the main Mesh Controller / Router
- Mark important usage points (bedroom, living room, office...)
- Walk around and discover positions that have good backhaul signal **and** can cover your usage points well
- Simple scoring system to suggest good placement spots

**Languages:** English + Tiếng Việt (more languages later if the community is interested)

---

## Mục tiêu dự án / Project Goals

### Tiếng Việt
Xây dựng một ứng dụng Android đơn giản, dễ dùng cho người không chuyên, giúp:
1. Đo cường độ sóng WiFi từ Mesh Controller hiện tại
2. Đánh dấu các vị trí sử dụng quan trọng trong nhà (phòng ngủ, phòng khách, bàn làm việc...)
3. Hướng dẫn người dùng di chuyển và tìm vị trí đặt node mesh phù hợp nhất (nhận sóng tốt từ controller + phát sóng tốt đến các điểm đã chọn)
4. Hỗ trợ cả trường hợp mesh dùng dây LAN (chỉ cần tối ưu vùng phủ)

Mục tiêu dài hạn: trở thành công cụ mã nguồn mở hữu ích cho cộng đồng, hỗ trợ đa ngôn ngữ.

### English
Build a simple, user-friendly Android app for non-experts that helps:
1. Measure WiFi signal strength from the current Mesh Controller/Router
2. Mark important usage points in the house (bedroom, living room, office...)
3. Guide the user to walk around and find the best positions to place a mesh node (good signal from controller + good coverage to selected points)
4. Also useful when mesh nodes are connected via Ethernet (focus on coverage only)

Long-term goal: become a useful open-source tool for the community with multi-language support.

---

## Roadmap / Các bước thực hiện

### Phase 1 – Foundation (Current)
- [x] Create GitHub repository
- [ ] Finalize Product Requirements Document (PRD)
- [ ] Research Android WiFi scanning limitations (Android 10+)
- [ ] Decide tech stack (Kotlin + Jetpack Compose recommended)
- [ ] Basic project structure + permission handling

### Phase 2 – Core Features (MVP)
- [ ] Real-time WiFi scan & RSSI display
- [ ] Create / manage measurement points (Controller + Usage points)
- [ ] Save measurement sessions
- [ ] "Find Node Position" mode with simple scoring
- [ ] Basic English + Vietnamese UI

### Phase 3 – Usability & Polish
- [ ] Better guidance & visual feedback while walking
- [ ] Session history & simple export
- [ ] Improve scoring algorithm
- [ ] Dark mode + better Material 3 UI

### Phase 4 – Advanced (Community-driven)
- [ ] Simple floor-plan / heatmap support
- [ ] Multi-band awareness (2.4 / 5 / 6 GHz)
- [ ] More languages
- [ ] Optional cloud sync or sharing reports

---

## Tech Stack (Proposed)

- **Language:** Kotlin
- **UI:** Jetpack Compose + Material 3
- **Architecture:** MVVM + Clean-ish structure
- **Local storage:** Room or DataStore
- **Min SDK:** 26 (Android 8.0) or higher depending on final decision
- **Target SDK:** Latest stable

---

## Development Notes

- Primary development & testing on real Android devices (emulator is not reliable for RSSI)
- Available machines: Windows + Ubuntu + Android phone
- AI assistants used for development: Grok, Codex (GPT), Deepseek (Pi Coding)

---

## Contributing

This project is in early stage. Contributions, ideas, translations and testing feedback are welcome once the MVP is usable.

---

## License

To be decided (likely MIT or Apache 2.0)

---

**Status:** Just created – planning & first commits coming soon.
