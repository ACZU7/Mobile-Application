# Parking Phase 1 evidence

Date: 2026-10-24. Feature owner: Omar Abdelatty.

Branch: `feature/parking-finder`, created from `feature/housing-repair-report` at `29657d2` following the instruction to implement features first and merge later. Parking implementation is recorded on this feature branch. No merge performed; PR and peer review remain pending.

## Automated verification

- `./gradlew.bat test`: BUILD SUCCESSFUL. 46 tests, zero failures or errors, including 3 Parking repository tests and 9 Parking ViewModel tests.
- `./gradlew.bat assembleDebug`: BUILD SUCCESSFUL; APK at `app/build/outputs/apk/debug/app-debug.apk`.
- After the final filter styling and annotation changes, `./gradlew.bat test assembleDebug`: BUILD SUCCESSFUL.
- After the shared-token bottom navigation and Parking arrow colour update, `./gradlew.bat test assembleDebug`: BUILD SUCCESSFUL again. Existing screenshots predate this colour-only update; the updated APK was installed on the emulator. Global colour tokens and Housing feature files were unchanged.
- Initial test attempt was blocked by Gradle cache permissions. First compiling run found an unsupported AutoMirrored ChevronRight icon; replaced with AutoMirrored ArrowForward and reran successfully.

## T03 emulator verification

The coding agent installed the debug APK and manually operated the running emulator using ADB taps, Android Back and UI hierarchy inspection. Device: `emulator-5554`, `sdk_gphone64_x86_64`, Android 17 / API 37. Screenshots below are actual captures from that running app.

1. Opened Parking and saw eight sample locations with a fictional-data notice: [list](01-list.png).
2. Selected Park & Ride and North Bristol; saw only Sample North Park & Ride: [filtered result](02-filtered.png).
3. Changed area to City centre; saw zero results, explanatory text and Clear filters: [empty state](03-empty.png).
4. Cleared filters and opened Sample Harbourside Court; verified name, type, area, address, opening information, price, spaces and sample disclaimer: [details](04-details.png).
5. Tapped Directions; saw the later-phase Maps message without launching navigation: [fallback dialog](05-directions-fallback.png).
6. Dismissed the dialog; Android Back returned from details to the list.
7. Opened Housing and confirmed its room-selection screen appeared. Returned to Parking and reopened details; UI hierarchy confirmed the Parking bottom navigation item was selected on the nested details screen.

T03 passed for the Phase 1 local sample-data scope. This does not verify real Maps hand-off or any live data.

## Remaining manual checks

- Android Studio Gradle sync and Run on the owner's selected AVD.
- Larger system fonts, landscape, tablet/second screen size, dark mode and RTL layout.
- Rotation and process recreation; selection is in memory and a missing selection has a return-to-list fallback.
- Peer review and PR evidence before merging.

No Housing test results were changed. T02 location sorting/permissions and later Maps integration remain outside Phase 1.

## Static map redesign — 2026-10-24

Rebuilt the finder to follow approved mockup screen 3. The map is a local Compose Canvas illustration: invented streets, parks and river, fixed sample pins, and a BRISTOL label. No map SDK, network source, API key, GPS or permissions were added. The search field shows the fixed prototype area, Bristol city centre; it is read-only in this static prototype.

Main filters are All, Multi-storey, Surface and EV charging, in that order. Type and area filters remain available through the settings icon. Pins and results share the filtered sample list; tapping a pin changes its highlight, while tapping a row opens details. Distances are fixed illustrative values, not calculated from the device. All selected bottom navigation items now use the shared teal primary token, following the latest approved-design instruction. Shared global colours and Housing feature files are unchanged.

Verification:

- `./gradlew.bat test assembleDebug`: BUILD SUCCESSFUL after implementing the map.
- `./gradlew.bat test`: BUILD SUCCESSFUL after adding six new finder tests. Total: 52 tests, zero failures/errors; 18 Parking tests.
- Installed and manually operated the APK on the API 37 emulator through ADB.
- Verified [initial static map and results](06-static-map.png), [location fallback](07-location-fallback.png), [EV charging filter](08-ev-filter.png), [matching row/details values](09-map-details.png) and [empty result](10-map-empty.png).
- Tapping the South Terrace pin changed the selected coral pin label to 80 spaces. Its row opened details showing the same 80 spaces and £2.50/hour. Android Back restored the filtered map.
- EV charging plus East Bristol produced no pins/results and a useful Clear filters action.

Earlier screenshots 01–05 document the previous list layout. Screenshots 06–10 document this map redesign. Large-font, landscape, tablet, RTL and dark-mode checks remain pending. Implementation is recorded on the feature branch; no merge or PR.

## Phase 3 directions and location sorting — 2026-11-09

Branch `feature/parking-directions-location`, from `main` at `9b78c5f`. This replaces the Phase 1 directions and location fallbacks with real device features, as set out in the implementation plan (Phase 3, Omar tasks 1–2).

**Directions.** Tapping Directions first shows a confirmation dialog, because the coordinates are fictional. Open Maps then tries three options in order: Google Maps directions to the car park's coordinates, any other app that handles `geo:` links, and finally the same directions URL in a browser. If none of these can open, a "Directions unavailable" dialog explains this, and the details screen stays open.

**Location sorting.** Permission is requested only when the user taps Find my location. Approximate location is accepted. The position comes from the Android framework `LocationManager`; no Google Play Services dependency was added. A cached position is used only if it is under two minutes old; otherwise the app asks for one fresh fix. When a position arrives, the list is sorted by straight-line (haversine) distance, the rows show calculated miles, the nearest pin is highlighted, the list scrolls back to the top, and the sheet notice changes to "Sorted by straight-line distance from you · fictional sample locations". The distance order is kept when filters change. If permission is denied, or no position can be read, the list keeps its previous order and a dialog explains the alternative.

| # | Check | Result | Evidence |
|---|---|---|---|
| 11 | Tapping Find my location shows the Android permission prompt; nothing was requested before the tap | Passed | [11-location-permission-prompt.png](11-location-permission-prompt.png) |
| 12 | Don't allow: "Location not allowed" dialog; default sample order and sample distances kept; browsing still works | Passed | [12-location-denied.png](12-location-denied.png) |
| 13 | Allowed, with a test location at 51.47, -2.52: Sample East Park & Ride first at 0.0 mi, then East Courtyard at 1.5 mi; nearest pin highlighted (150 spaces) | Passed after fix (see below) | [13-location-sorted.png](13-location-sorted.png) |
| 14 | Directions on Sample East Courtyard shows the confirmation with the fictional-location warning | Passed | [14-directions-confirm.png](14-directions-confirm.png) |
| 15 | Open Maps launches Google Maps in driving-directions mode with destination 51.4600000,-2.5500000, matching the sample coordinates | Passed | [15-directions-google-maps.png](15-directions-google-maps.png) |
| 16 | Google Maps and Chrome temporarily disabled: "Directions unavailable" dialog; no crash. Both apps re-enabled afterwards | Passed | [16-directions-unavailable.png](16-directions-unavailable.png) |
| 17 | Device location turned off: "Location unavailable" dialog; the earlier sort is kept | Passed | [17-location-unavailable.png](17-location-unavailable.png) |

Issues found on the emulator and fixed before the final run:

1. **Stale cached location.** The first version trusted any cached position, so the list was sorted from the emulator's default Mountain View fix (about 5,293 mi). Fixed by accepting only cached positions under two minutes old.
2. **Nearest results scrolled out of view.** The keyed `LazyColumn` kept the previous first row on screen after re-sorting, so the two nearest car parks sat above the visible area. Fixed by scrolling to the top once for each new sort, using a sort counter. Returning from the details screen does not reset the scroll.

Test setup note: on the API 37 emulator, `adb emu geo fix` reported OK but did not change the reported position. The Bristol test position was therefore set through a shell test provider (`cmd location providers set-test-provider-location gps`).

Verification: `./gradlew.bat test assembleDebug` BUILD SUCCESSFUL; 126 unit tests, zero failures or errors. 27 Parking tests in total: 23 ViewModel, 4 directions/distance; 10 are new. Tested on the Medium Phone API 37 emulator, operated by the coding agent through ADB for Omar Abdelatty. Large font, landscape, dark mode, RTL and tablet checks were not repeated for these dialogs. Peer review is pending.
