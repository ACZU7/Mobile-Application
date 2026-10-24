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
