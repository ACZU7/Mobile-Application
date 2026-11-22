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
| QR02 | 2026-11-19 | Passed: the rent account shows sample balance, next payment (1 December 2026, £520.00) and history, all labelled as sample data. Turning on "Remind me before rent is due" and allowing notifications set a WorkManager reminder for 28 November 2026 09:00 (3 days before) and showed "Reminder set for 28 November 2026". After force-stopping and reopening the app the switch was still on and the reminder still scheduled. Turning it off cancelled the reminder and stayed off after reopening. The debug-only "show test reminder now" button showed the notification "Rent due soon (sample data)", which says it is sample data. | Mohammad AlAbwah | [switch off](evidence/rent-account/QR02-01-reminder-off.png), [reminder set](evidence/rent-account/QR02-02-reminder-on.png), [payment history](evidence/rent-account/QR02-03-payment-history.png), [test notification](evidence/rent-account/QR02-04-test-notification.png); after-restart screenshot to be added |
| QR03 (Housing repairs) | 2026-11-19 | Passed: submitted a Kitchen / Leak or water damage repair (HR-2026-0004, morning appointment); after force-stopping and reopening the app it was still listed in My repairs with status Submitted. | Mohammad AlAbwah | [Housing home, 4 saved repairs](evidence/rent-account/QR03-01-housing-home.png); after-restart screenshot to be added |
| T10 (rent reminder notifications) | 2026-11-19 | Passed: no permission is asked when the rent screen opens, only when the switch is turned on. Denied: the switch turned back off and a "Notifications are off" explanation appeared with "Open settings" and "Not now"; the rent screen stayed usable, no crash. Allowed: the reminder was set. | Mohammad AlAbwah | [permission prompt](evidence/rent-account/T10-01-permission-prompt.png); denied-state screenshot to be added |

## Evidence to retain

- Screenshots/recordings for each completed test.
- Issues found, owner, fix and retest outcome.
- Commit links for the feature that was tested.

## Phase 4 baseline — 2026-11-22

This subsection is the initial checkpoint. The follow-up below supersedes its open-gap and no-device statements.

Baseline: merged `main` commit `1887c29` (PR #5). Verification branch: `feature/phase-4-verification`. This is the start of final verification, not a full Phase 4 pass.

- `./gradlew.bat test assembleDebug lintDebug`: BUILD SUCCESSFUL. The unit-test task was up to date; its existing XML results contain 154 tests and zero failures. No fresh execution of all tests is claimed.
- Android lint: no errors; warnings remain for dependency/target versions, plural resources, unused resources, Compose resource access and KTX suggestions. Reports are generated in `app/build/reports/lint-results-debug.html` and `.xml` (local build outputs).
- `adb devices -l`: no connected devices. No new manual UI, accessibility, permission, offline-map or second-device pass is claimed for this baseline.
- Mohammad's `feature/rent-account-persistence` branch is not included in this baseline. Repeat integration checks after its review and merge.

| Follow-up | Owner | Status / acceptance evidence needed |
|---|---|---|
| FR01–FR03 / QR01: Parking and permit regression | Omar | Repeat filters, selection/details, location allowed/denied/off, directions fallback, reminder allowed/denied/cancel and persistence on the final build. |
| FR04–FR05: Streets regression | Omar | Repeat invalid/valid submissions, camera/gallery/denial, photo removal, references, status/filter/empty states and Back on the final build. |
| QR03: Street Report persistence | Omar | Open gap: `StreetReportRepository` is in memory, so submitted reports disappear after process death. Resolve and test restart recovery before claiming report persistence. |
| T09 / NFR01 / NFR03 | Both, own features | Final-build large-font, landscape, dark-mode, RTL and second-screen-size evidence; older screenshots predate the complete Phase 3 build. |
| Offline maps | Omar | Verify a usable no-network path and preserved report/parking actions. |
| Reminder reboot and camera hardware | Omar | Verify reboot scheduling, camera switching and flash on supporting hardware; distinguish forced notification delivery from natural scheduling. |
| Housing/rent integration | Mohammad / both | Review and integrate the Phase 3 branch, then run shared navigation and combined build/tests. |
| Final records/video | Both | Update merged PR references, retain final evidence and prepare each member's implementation explanation. |


## Phase 4 follow-up — 2026-11-22

Locally combined Mohammad's Phase 3 branch in merge `7f21003` on `feature/phase-4-verification`; main is unchanged. Added Room-backed Street Reports and a non-destructive database version 1-to-2 migration. Saving must succeed before confirmation; failed writes retain form/photos and show an error. Landscape Parking and permit screens now use adjacent map/results panels. This supersedes the baseline's in-memory Street Report and no-device findings.

| Scope | Result |
|---|---|
| Build/unit/lint | Debug and release builds pass; 194 unit tests, zero failures/errors; lint has warnings but no errors. |
| QR03 | Real Room database reopen restores reports and photo URI lists; version-one migration retains an existing Housing repair and permits Street submissions. Reference numbering continues after reload. |
| T01–T05 / QR01 / T10 | Core Parking/Streets/permit journeys pass on the combined emulator build; separate runs pass denied camera/location/notifications, allowed camera capture/switch/remove and allowed location sorting. Reminder saved/scheduled state survives an emulator reboot and switch-off cancels it. |
| T06–T08 | Housing invalid/valid wizard and progress pass. CAZ empty validation and no-charge/charge/unknown results show prototype statements and an official-checker action. This does not claim Housing photo support or repeat external browser hand-off. |
| T09 | Core journeys pass at 150% font with dark mode, simulated tablet size, verified RTL, and actual landscape with networking disabled. Same API 37 emulator; no independent device/OS result claimed. |

Full screenshots, test methods, configuration limits, defect/retest notes and assessment walkthrough preparation: [Phase 4 evidence](evidence/phase4/README.md). The default device suite skips parameter-dependent tests; these were executed separately with their corresponding permission/reboot setup.

Final sign-off remains open for human review/merge, shared Home/mockup completion, agreement on existing Housing photo scope, fresh gallery/external Directions checks, physical-device/accessibility checks where available, and the students' assessment recording. The locally combined build is not a claim that main already contains Mohammad's branch.

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

Omar Abdelatty, branch `feature/parking-directions-location` (PR #5). Tested by the coding agent operating the API 37 emulator through ADB.

| ID | Actual result | Evidence |
|---|---|---|
| T02 / T03 (map) | Passed. The Car park finder uses real OpenStreetMap tiles: pins at the sample car-park positions, pin selection, the user's location dot after Find my location, and the OSM credit. Dark mode passed. | [Parking evidence 26–29](evidence/parking/README.md#real-openstreetmap-maps--2026-11-09) |
| Permit zone map | Passed. The fictional Zone CM is drawn around the sample address on real tiles, with a not-a-real-boundary banner. | [Parking evidence 28](evidence/parking/README.md#real-openstreetmap-maps--2026-11-09) |
| T04 (location map) | Passed. The report form shows a fixed real map with the sample report pin. | [Street Issues evidence 41](evidence/streetissues/README.md#real-openstreetmap-location-map--2026-11-09) |

`./gradlew.bat test assembleDebug`: BUILD SUCCESSFUL; 154 unit tests, zero failures or errors.
