# MVP test plan

## Test approach

Test every acceptance criterion manually on the primary emulator. Repeat key tests on a second Android screen size and, if available, a physical Android device. Keep screenshots or short screen recordings as evidence.

## Core test cases

| ID | Test | Expected result | Owner |
|---|---|---|---|
| T01 | Launch the app and use every navigation destination. | All destinations open; back navigation is sensible; no crash. | Omar Abdelatty + Mohammad AlAbwah |
| T02 | Open Parking with location allowed and denied. | Results sort by distance when allowed; an understandable fallback appears when denied. | Omar Abdelatty |
| T03 | Apply parking filters and open a car park. | Filtered list updates; details show; directions hand-off works or provides a fallback. | Omar Abdelatty |
| T04 | Submit a valid street report. | Category, text, optional photo and editable location are captured; confirmation/reference/status appear. | Omar Abdelatty |
| T05 | Submit an incomplete street report. | Required-field validation clearly explains what must be corrected. | Omar Abdelatty |
| T06 | Submit a valid housing repair. | The repair is confirmed with selected room, problem and appointment preference. | Mohammad AlAbwah |
| T07 | Open housing repair progress. | Saved repair data and demonstration status are shown. | Mohammad AlAbwah |
| T08 | Enter valid and invalid CAZ registrations. | Validation is clear; every result states that it is a prototype result and provides an official-checker action. | Mohammad AlAbwah |
| T09 | Test large font size and a second screen size. | Core labels/actions remain visible, usable and not clipped. | Omar Abdelatty + Mohammad AlAbwah |
| T10 | Deny camera/notification permissions. | The affected flow remains usable and gives an appropriate explanation. | Omar Abdelatty + Mohammad AlAbwah |

## Results

| ID | Date | Result | Tested by | Evidence |
|---|---|---|---|---|
| T03 | 2026-10-24 | Passed for static-map redesign: EV chip updates pins/results; pin selection changes spaces label; row opens matching details; Back restores finder; settings filters produce an empty state; location button provides later-phase fallback. | Omar Abdelatty feature; coding agent operating API 37 emulator via ADB | [Static map screenshots 06–10 and verification](evidence/parking/README.md#static-map-redesign--2026-10-24) |
| T03 | 2026-10-24 | Passed for Phase 1 sample-data scope: combined filters, empty state, clear filters, details, Directions fallback and Android Back. Parking selected on details; Housing still reachable. | Omar Abdelatty feature; verified by coding agent operating emulator through ADB | [Parking test record and five screenshots](evidence/parking/README.md) |
| T06 | 2026-10-22 | Passed | Mohammad AlAbwah | [08-T06-confirmation.png](evidence/housing/08-T06-confirmation.png) |
| T07 | 2026-10-22 | Passed | Mohammad AlAbwah | [09-T07-progress.png](evidence/housing/09-T07-progress.png) |
| T08 | 2026-10-31 | Passed: empty and invalid (`AB-12`) registrations show a clear error with an icon; no-charge (IB15 PTL), car charge (IC05 PTL), HGV charge (IF12 HGV) and unknown (AB12 CDE) results each show the prototype statement and "Use the official checker"; the button opens the council checker in the browser. | Mohammad AlAbwah | [empty](evidence/clean-air-zone/01-empty-input.png), [empty error](evidence/clean-air-zone/02-empty-error.png), [invalid](evidence/clean-air-zone/03-invalid-error.png), [no charge](evidence/clean-air-zone/04-no-charge.png), [car charge](evidence/clean-air-zone/05-charge-car.png), [HGV charge](evidence/clean-air-zone/06-charge-hgv.png), [unknown](evidence/clean-air-zone/07-unknown.png), [official checker](evidence/clean-air-zone/08-official-checker.png) |

## Evidence to retain

- Screenshots/recordings for each completed test.
- Issues found, owner, fix and retest outcome.
- Commit links for the feature that was tested.


## Phase 2 Street Issues automated verification ? 2026-10-30

Omar Abdelatty feature, branch `feature/street-issue-report`: `./gradlew.bat test` passed (67 tests, zero failures/errors, including 15 Street Issues tests); `./gradlew.bat assembleDebug` passed. Automated coverage includes T04/T05 validation/submission logic, references, repository retrieval, status filters and empty results. T04/T05 manual UI acceptance remains pending: ADB found no connected emulator/device. No manual pass or screenshot evidence is claimed. See [verification and pending checks](evidence/streetissues/README.md).

## Phase 2 Street Issues emulator follow-up ? 2026-10-30

This supersedes the earlier no-device/pending-manual note for the Street Issues scope. Tested by the coding agent operating the API 37 emulator for Omar Abdelatty.

| ID | Actual result | Evidence |
|---|---|---|
| T04 / FR05 | Passed local submit, edited fictional location, generated reference, confirmation disclaimer, status timeline, My Reports filtering and empty Fixed results. Final build references SR-2026-0004 and SR-2026-0005 verified. | [Street Issues test record and screenshots](evidence/streetissues/README.md#manual-emulator-results) |
| T05 | Passed empty category/description, 5-character minimum error, 501-character maximum error, and correction followed by successful submission. | [Validation evidence](evidence/streetissues/README.md#manual-emulator-results) |
| T01 (integration smoke) | Passed nested Streets selection, Back, reset form and tab restoration; Home/Parking/Housing remained reachable. Existing feature acceptance was not fully repeated. | Screenshots 14-18 in [evidence](evidence/streetissues/README.md) |
| T09 (Streets scope) | Passed 150% font size, landscape, dark mode, forced RTL, and simulated tablet size 1600 x 2560 / 240 dpi on the same emulator; draft retained on rotation. Separate API 29 tablet AVD stayed offline; no second-device/OS pass claimed. | Screenshots 19-32 in [evidence](evidence/streetissues/README.md) |

Visual testing found and fixed disclaimer punctuation and selected-filter colour; final `./gradlew.bat test assembleDebug` passed (67 tests, zero failures/errors; BUILD SUCCESSFUL in 3s). Shared theme/components and Housing/CAZ files are unchanged. Human peer review remains outstanding.

## Phase 3 Parking directions and location — 2026-11-09

Omar Abdelatty feature, branch `feature/parking-directions-location`. Tested by the coding agent operating the API 37 emulator through ADB.

| ID | Actual result | Evidence |
|---|---|---|
| T02 | Passed. Permission is requested only from Find my location. Denied: default order kept and an explanation shown. Allowed (test location 51.47, -2.52): nearest-first order with calculated miles and the nearest pin highlighted. Location turned off: "Location unavailable" explanation. | [Parking evidence 11–13, 17](evidence/parking/README.md#phase-3-directions-and-location-sorting--2026-11-09) |
| T03 (directions) | Passed. The confirmation dialog warns that the location is fictional; Open Maps launches Google Maps directions to the selected car park's coordinates. With Maps and Chrome disabled, a "Directions unavailable" fallback appears and the app does not crash. | [Parking evidence 14–16](evidence/parking/README.md#phase-3-directions-and-location-sorting--2026-11-09) |

`./gradlew.bat test assembleDebug`: BUILD SUCCESSFUL; 126 unit tests, zero failures or errors.

## Phase 3 resident's permit and reminder — 2026-11-09

Omar Abdelatty feature, branch `feature/parking-directions-location` (PR #5). Tested by the coding agent operating the API 37 emulator through ADB.

| ID | Actual result | Evidence |
|---|---|---|
| T10 (notifications, Parking scope) | Passed. Notification permission is requested only when the reminder is turned on. If it is denied, the switch stays off, an explanation and an Open settings action are shown, and the permit screen is still usable. Once allowed, the reminder is scheduled and the notification is delivered (job force-run). | [Parking evidence 20–22, 24](evidence/parking/README.md#phase-3-permit-zone-checker-and-expiry-reminder--2026-11-09) |
| Persistence after restart (Parking scope) | Passed. After a force-stop and relaunch, the stored sample permit and the reminder choice are restored from DataStore. | [Parking evidence 23](evidence/parking/README.md#phase-3-permit-zone-checker-and-expiry-reminder--2026-11-09) |
| T09 (permit screen) | Passed in dark mode at 150% font on the phone emulator; a clipped map label was fixed. Tablet and RTL were not tested for this screen. | [Parking evidence 25](evidence/parking/README.md#phase-3-permit-zone-checker-and-expiry-reminder--2026-11-09) |

`./gradlew.bat test assembleDebug`: BUILD SUCCESSFUL; 141 unit tests, zero failures or errors. Camera-permission T10 is still pending; it depends on the optional Street Issue photo task.

## Phase 3 Street Issues photos — 2026-11-09

Omar Abdelatty feature, branch `feature/parking-directions-location` (PR #5). Tested by the coding agent operating the API 37 emulator through ADB.

| ID | Actual result | Evidence |
|---|---|---|
| T04 (optional photo) | Passed. A photo can be taken with the in-app camera or chosen from the gallery, removed, and submitted with the report. The confirmation and My reports screens show the thumbnail. Reports without a photo still submit. | [Street Issues evidence 33, 36–40](evidence/streetissues/README.md#phase-3-optional-photos--2026-11-09) |
| T10 (camera) | Passed. Camera permission is requested only after Add photo is tapped. If it is denied, "Camera not allowed" offers Choose from gallery and Continue without photo; the gallery path still attached a photo and the report could be finished. | [Street Issues evidence 34–36](evidence/streetissues/README.md#phase-3-optional-photos--2026-11-09) |

`./gradlew.bat test assembleDebug`: BUILD SUCCESSFUL; 149 unit tests, zero failures or errors. Together with the notification results above, T10 is now covered for Omar's features.

## Real maps — 2026-11-09

Omar Abdelatty, branch `feature/real-maps`. Tested by the coding agent operating the API 37 emulator through ADB.

| ID | Actual result | Evidence |
|---|---|---|
| T02 / T03 (map) | Passed. The Car park finder uses real OpenStreetMap tiles: pins at the sample car-park positions, pin selection, the user's location dot after Find my location, and the OSM credit. Dark mode passed. | [Parking evidence 26–29](evidence/parking/README.md#real-openstreetmap-maps--2026-11-09) |
| Permit zone map | Passed. The fictional Zone CM is drawn around the sample address on real tiles, with a not-a-real-boundary banner. | [Parking evidence 28](evidence/parking/README.md#real-openstreetmap-maps--2026-11-09) |
| T04 (location map) | Passed. The report form shows a fixed real map with the sample report pin. | [Street Issues evidence 41](evidence/streetissues/README.md#real-openstreetmap-location-map--2026-11-09) |

`./gradlew.bat test assembleDebug`: BUILD SUCCESSFUL; 154 unit tests, zero failures or errors.
