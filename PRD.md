# MeshScout — Product Requirements Document (PRD)
**Version:** 1.0  
**Date:** 2026-08-17  
**Status:** Ready for implementation / Open Source  
**Repo:** https://github.com/gonegirl07/mesh-scout  
**Authors:** Product + Technical Analyst (community)  
**Languages:** English (primary) + Tiếng Việt  

---

## 1. Overview / Tổng quan

### English
**MeshScout** is an open-source Android application that helps non-technical users find the optimal placement for Wi-Fi Mesh Nodes inside a home or small office.

Unlike classic Wi-Fi Analyzer apps that only show a list of networks and signal strength, MeshScout guides the user through a structured measurement workflow:

1. Create a measurement session  
2. Mark the current Mesh Controller / Router location  
3. Mark important usage points (bedroom, living room, desk…)  
4. Enter “Find Node Position” mode – walk around the house while the app shows real-time RSSI from the Controller + a simple score that indicates good placement  
5. Save history for later comparison  

The app is bilingual (English + Vietnamese) from day one, with potential community-driven language expansion later.

### Tiếng Việt
**MeshScout** là ứng dụng Android mã nguồn mở giúp người dùng không chuyên kỹ thuật tìm vị trí đặt Mesh Node tối ưu trong nhà hoặc văn phòng nhỏ.

Khác với các app WiFi Analyzer thông thường chỉ liệt kê mạng và cường độ sóng, MeshScout hướng dẫn người dùng theo quy trình đo có cấu trúc:

1. Tạo phiên đo  
2. Đánh dấu vị trí Mesh Controller / Router hiện tại  
3. Đánh dấu các điểm sử dụng quan trọng (phòng ngủ, phòng khách, bàn làm việc…)  
4. Vào chế độ “Tìm vị trí Node” – đi quanh nhà, app hiển thị RSSI realtime từ Controller + điểm số đơn giản gợi ý vị trí tốt  
5. Lưu lịch sử đo  

Ứng dụng hỗ trợ song ngữ (English + Tiếng Việt) ngay từ đầu.

---

## 2. Goals / Mục tiêu

### Primary Goals (MVP)
| Goal | Description (EN) | Mô tả (VI) |
|------|------------------|------------|
| Guided measurement | Help non-experts systematically measure and decide mesh node placement | Giúp người không chuyên đo và quyết định vị trí đặt mesh node một cách có hệ thống |
| Real-time feedback | Show live RSSI of the controller + simple placement score while walking | Hiển thị RSSI realtime của controller + điểm số vị trí khi đi quanh nhà |
| Privacy-first | No location data leaves the device; no accounts required | Không gửi dữ liệu vị trí ra ngoài; không cần tài khoản |
| Bilingual UX | Full English + Vietnamese support | Hỗ trợ đầy đủ English + Tiếng Việt |
| Open source | Clean, documented, contribution-friendly codebase | Mã nguồn sạch, có tài liệu, dễ đóng góp |

### Non-Goals (MVP)
- Floor plan drawing / heatmaps
- Automatic mesh topology recommendation across multiple nodes
- Cloud sync or account system
- Support for Wi-Fi 6E/7 specific features beyond basic RSSI
- iOS version

---

## 3. User Personas / Chân dung người dùng

### Persona 1: Home User (Primary)
- **Name:** Minh (32), office worker, lives in apartment with family  
- **Tech level:** Low–medium. Knows how to install a mesh system but struggles with optimal placement.  
- **Pain:** After installing mesh, some rooms still have weak signal. Manufacturer app only shows “good/fair/poor” without guidance.  
- **Goal:** Spend 10–15 minutes walking around the house and know exactly where to put the second node.

### Persona 2: Small Office / Homestay Owner
- Needs reliable coverage in multiple rooms without hiring an installer.  
- Values history of measurements to compare before/after adding a node.

### Persona 3: Enthusiast / Contributor
- Wants to help improve the scoring algorithm or add more languages.

---

## 4. User Stories / User Stories

### Epic: Measurement Session
1. As a user, I want to create a new measurement session with a name and optional notes so I can later distinguish different attempts.
2. As a user, I want to mark the current position of my Mesh Controller / main Router (by simply standing there and tapping “Mark Controller”).
3. As a user, I want to mark several important usage points (e.g. “Bed”, “Desk”, “Sofa”) so the app knows which areas need good coverage.
4. As a user, I want to enter “Find Node Position” mode, walk around my home, and see live signal strength from the Controller + a simple score telling me if the current spot is good for placing a node.
5. As a user, I want to save a recommended position (or multiple candidates) with timestamp and notes.
6. As a user, I want to view history of past sessions and compare scores.

### Epic: Permissions & Reliability
7. As a user, I want clear explanations why the app needs Location / Nearby Wi-Fi permissions (in both languages).
8. As a user, I want the app to work reasonably well even with Android scan throttling (no silent failures).

### Epic: Localization
9. As a Vietnamese user, I want the entire UI and guidance texts in Vietnamese.
10. As an English user, I want the same experience in English.

---

## 5. Functional Requirements / Yêu cầu chức năng

### 5.1 MVP (Must-have)

| ID | Requirement (EN) | Yêu cầu (VI) | Priority |
|----|------------------|--------------|----------|
| F1 | Create / list / delete measurement sessions | Tạo / liệt kê / xóa phiên đo | P0 |
| F2 | Mark Controller position (stores current RSSI baseline + BSSID/SSID) | Đánh dấu vị trí Controller | P0 |
| F3 | Add / edit / remove usage points (label + optional RSSI at that point) | Thêm / sửa / xóa điểm sử dụng | P0 |
| F4 | “Find Node Position” live mode: continuous (throttled) scanning of Controller BSSID, display live RSSI + placement score | Chế độ tìm vị trí Node realtime | P0 |
| F5 | Simple placement score (see section 6) | Điểm số vị trí đơn giản | P0 |
| F6 | Save candidate positions with score, RSSI, timestamp, note | Lưu vị trí ứng viên | P0 |
| F7 | Session history list + basic detail view | Lịch sử phiên đo | P0 |
| F8 | Full bilingual UI (EN / VI) with language switch | Giao diện song ngữ + chuyển ngôn ngữ | P0 |
| F9 | Clear permission rationale screens + graceful degradation when permissions denied | Màn hình giải thích quyền + xử lý khi bị từ chối | P0 |
| F10 | Detect and show warning when scan throttling is active | Cảnh báo khi bị throttling | P1 |

### 5.2 Later / Nice-to-have
- Floor-plan overlay (manual grid or photo background)
- Multi-node simulation (estimate coverage after placing 2nd/3rd node)
- Export session as JSON / CSV
- Community scoring models
- More languages (via Crowdin / Weblate)
- Widget or quick-measure mode
- Dark theme refinements & Material 3 expressive

---

## 6. Placement Scoring Proposal / Đề xuất thuật toán điểm số

### Simple & Explainable Score (MVP)

We deliberately keep the score **transparent and easy to understand** for non-experts.

**Inputs at current position:**
- `RSSI_controller` (dBm) – signal from the Mesh Controller / main AP (matched by BSSID preferred, fallback SSID)
- Optional: average RSSI observed at the previously marked usage points (if user walked to them)

**Score formula (0–100):**

```
score = clamp(0, 100,
    40 * normalize(RSSI_controller, -90, -40)     // strong link to controller is critical
  + 30 * coverage_bonus                           // how well it can serve the usage points
  + 20 * balance_factor                           // avoid being too close to controller
  + 10 * stability                                // low variance across last N readings
)
```

**Normalization helper:**
```
normalize(rssi, min, max) = (rssi - min) / (max - min)   // clamped 0..1
```

**Rules of thumb shown to user:**
- Excellent (80–100): Strong signal from controller **and** good potential coverage of usage points. Ideal zone.
- Good (60–79): Acceptable.
- Fair (40–59): Possible but not optimal.
- Poor (<40): Too weak or too close / unbalanced.

**Implementation notes:**
- Prefer matching exact BSSID of the controller (user can confirm which AP is the controller).
- Average last 3–5 successful scans to reduce noise.
- Show both raw RSSI (dBm) and the score so power users can override.
- Never claim “guaranteed perfect coverage” – always frame as “recommended starting position”.

### Tiếng Việt (tóm tắt)
Điểm số 0–100 dựa trên:
- Cường độ sóng từ Controller (quan trọng nhất)
- Khả năng phủ các điểm sử dụng đã đánh dấu
- Không quá gần Controller
- Độ ổn định của tín hiệu

Người dùng thấy cả RSSI thô và điểm số dễ hiểu.

---

## 7. Non-Functional Requirements / Yêu cầu phi chức năng

| Category | Requirement |
|----------|-------------|
| Performance | UI remains responsive; scan loop must not block main thread |
| Battery | Scanning only while app is in foreground and user is in “Find” mode. Clear stop button. |
| Privacy | No analytics by default. No transmission of BSSID/SSID/RSSI off-device. |
| Accessibility | Support TalkBack, sufficient contrast, large touch targets |
| Localization | All user-facing strings in `strings.xml` (EN + VI). RTL not required for MVP. |
| Min SDK | 26 (Android 8.0) – covers vast majority of active devices in 2026 |
| Target SDK | 35 or 36 (Android 15/16) at time of first release |
| Architecture | Kotlin + Jetpack Compose + recommended Architecture Components |

---

## 8. Technical Constraints & Research Summary  
### Android Wi-Fi Scanning Limitations (Android 10 → 15/16, 2026)

#### 8.1 Permissions (critical)

| Permission | When required | Notes |
|------------|---------------|-------|
| `ACCESS_FINE_LOCATION` | Targeting API 29+ for `startScan()` / `getScanResults()` | Still required for reliable scan results even on Android 13+. Location services must be **enabled** on the device. |
| `NEARBY_WIFI_DEVICES` | Android 13+ (API 33+) | Preferred for “nearby Wi-Fi devices”. Use `android:usesPermissionFlags="neverForLocation"` if the app does not derive physical location. |
| `CHANGE_WIFI_STATE` | Starting scans | Normal permission |
| `ACCESS_WIFI_STATE` | Reading results | Normal permission |

**Recommended manifest strategy:**
```xml
<!-- For Android 12 and below -->
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"
                 android:maxSdkVersion="32" />

<!-- For Android 13+ -->
<uses-permission android:name="android.permission.NEARBY_WIFI_DEVICES"
                 android:usesPermissionFlags="neverForLocation" />
```

In practice, many production scanner apps still request `ACCESS_FINE_LOCATION` on Android 13+ because `getScanResults()` documentation continues to list it as required for full results. MeshScout should request the minimal set that works reliably and explain clearly in both languages why the permission is needed (“to measure Wi-Fi signal strength of your router, not to track your GPS location”).

#### 8.2 Scan Throttling (unchanged since Android 9)

- **Foreground app:** maximum **4 scans every 2 minutes**.
- **Background apps (combined):** 1 scan every 30 minutes.
- Developer option exists to disable throttling for testing only (`Developer options → Networking → Wi-Fi scan throttling`).
- Consequence for MeshScout: true “real-time” continuous scanning is impossible under normal user conditions. Design must:
  - Work in short bursts + display last known good reading
  - Educate the user to move slowly
  - Show a visible indicator when the next scan is allowed
  - Prefer listening to system scan results (`SCAN_RESULTS_AVAILABLE_ACTION`) when possible

#### 8.3 Obtaining accurate RSSI

- Primary API: `WifiManager.startScan()` → wait for `SCAN_RESULTS_AVAILABLE_ACTION` → `getScanResults()`.
- RSSI value: `ScanResult.level` (dBm).
- Best practices:
  - Match by BSSID of the controller (most reliable).
  - Average several consecutive readings.
  - Ignore results older than a few seconds.
  - Connected `WifiInfo.getRssi()` only works for the currently associated network and is less useful when the user is far from the controller.

#### 8.4 Other 2025–2026 notes
- Android 16 introduces opt-in Local Network restrictions (related to `NEARBY_WIFI_DEVICES`); full enforcement expected in Android 17.
- Android Vitals flags apps that perform > 4 Wi-Fi scans per hour in the background → keep all scanning foreground-only.
- No public high-frequency RSSI API without being connected to the AP.

**Implication for product:** The UX must set correct expectations (“walk slowly, wait for the score to update”) rather than promising a continuous heatmap.

---

## 9. Proposed Project Structure / Cấu trúc thư mục đề xuất

**Package name (recommended):**  
`com.meshscout.app`  
(or `io.github.gonegirl07.meshscout` if stronger GitHub association is desired)

```
app/
├── src/main/
│   ├── java/com/meshscout/app/
│   │   ├── MeshScoutApplication.kt
│   │   ├── MainActivity.kt
│   │   ├── di/                     # Hilt / Koin modules
│   │   ├── data/
│   │   │   ├── local/              # Room entities, DAOs, database
│   │   │   ├── repository/
│   │   │   └── wifi/               # WifiScanner, ScanResultMapper, ThrottleHelper
│   │   ├── domain/
│   │   │   ├── model/              # Session, UsagePoint, CandidatePosition, Score
│   │   │   ├── usecase/
│   │   │   └── repository/         # interfaces
│   │   ├── ui/
│   │   │   ├── theme/
│   │   │   ├── navigation/
│   │   │   ├── session/            # list, create, detail
│   │   │   ├── measure/            # mark controller, mark points, live find mode
│   │   │   ├── history/
│   │   │   ├── permissions/
│   │   │   └── components/         # reusable Composables (ScoreGauge, RssiBadge…)
│   │   └── util/
│   ├── res/
│   │   ├── values/                 # strings.xml (EN)
│   │   ├── values-vi/              # strings.xml (VI)
│   │   └── ...
│   └── AndroidManifest.xml
├── build.gradle.kts
└── ...
```

**Recommended stack:**
- Kotlin 2.x
- Jetpack Compose + Material 3
- Navigation Compose
- Room (local persistence)
- Hilt or Koin
- Coroutines + Flow
- Accompanist or native permission APIs

---

## 10. Success Metrics / Chỉ số thành công

| Metric | Target (first 6 months) |
|--------|-------------------------|
| GitHub stars | ≥ 150 |
| Releases with working APK | At least 3 public releases |
| Crash-free sessions | ≥ 99% |
| User feedback (issues / discussions) | Clear confirmation that the guided flow is understandable by non-experts |
| Bilingual completeness | 100% of user-facing strings translated |
| Average time to first useful recommendation | < 12 minutes (self-reported) |

---

## 11. Recommended GitHub Issues (Priority order)

Create these issues immediately after committing the PRD:

### High Priority (P0 – do first)
1. **[Epic] Project scaffolding** – Create Android project with Compose, package `com.meshscout.app`, basic navigation, bilingual string resources, README.
2. **[Research] Confirm Wi-Fi permission matrix on Android 13–16** – Test `NEARBY_WIFI_DEVICES` + `neverForLocation` vs `ACCESS_FINE_LOCATION` for `getScanResults()` on real devices.
3. **[Feature] Session model + Room database** – Session, UsagePoint, CandidatePosition entities + DAOs.
4. **[Feature] Permission rationale & request flow** – Beautiful bilingual screens explaining why Location / Nearby Wi-Fi is needed.
5. **[Feature] Basic WifiScanner wrapper** – Handle throttling, BSSID matching, average RSSI, expose Flow of current reading.
6. **[Feature] Mark Controller screen**
7. **[Feature] Mark Usage Points screen**
8. **[Feature] Live “Find Node Position” screen** – RSSI + score gauge + save candidate.
9. **[Feature] Simple scoring engine** (implement section 6 formula).
10. **[Feature] Session history list + detail**.

### Medium Priority (P1)
11. Throttling detection & user-facing warning.
12. Language switcher (in-app, not only system).
13. Export session as JSON.
14. Basic unit tests for scoring + repository.
15. CI (GitHub Actions) – build + lint.

### Low Priority / Later
16. Floor-plan / photo background experiment.
17. Multi-language infrastructure (Weblate).
18. Play Store listing preparation (screenshots, description EN/VI).
19. Accessibility audit.
20. Dark theme polish.

---

## 12. References (Primary Sources)

- [Wi-Fi scanning overview](https://developer.android.com/develop/connectivity/wifi/wifi-scan) — Android Developers  
- [Request permission to access nearby Wi-Fi devices](https://developer.android.com/develop/connectivity/wifi/wifi-permissions) — Android Developers  
- [Excessive Wi-Fi Scanning in the Background](https://developer.android.com/topic/performance/vitals/bg-wifi) — Android Developers  
- Android 9–16 behavior confirmed via official docs and community reports (2025–2026)

---

**Document status:** Ready to be committed as `PRD.md` in the root of the repository.  
Next step: Create the high-priority issues listed above and begin scaffolding.

---

*MeshScout – Measure. Walk. Place. Better Wi-Fi for everyone.*  
*Đo. Đi. Đặt. Wi-Fi tốt hơn cho mọi người.*
