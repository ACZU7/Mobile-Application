# Contributions record

Update this file throughout the project. The final mini report should refer to the evidence here and be agreed/signed by both group members.

## Ownership

| Owner | Primary responsibilities |
|---|---|
| Omar Abdelatty | Parking finder, filters, car-park details/directions, street issue report, street-report status, parking permit reminder. |
| Mohammad AlAbwah | Housing repair report/progress, Clean Air Zone checker, rent account/reminder, local data persistence for their features. |
| Omar Abdelatty + Mohammad AlAbwah | Dashboard, Navigation Compose, theme/design components, integration, testing, design document and video. |

## Contribution log

| Date | Owner | Work completed | Files/PR/commit evidence | Reviewed by |
|---|---|---|---|---|
|  | Omar Abdelatty |  |  |  |
| 2026-10-24 | Omar Abdelatty | Redesigned Parking to approved mockup screen 3: local Compose map illustration, fixed teal/coral pins, four category/EV filter chips, settings for type/area filters, location fallback and rounded sample-results sheet linked to details. Added six finder tests; all 52 unit tests and debug build passed. Manually checked map controls, details, Back and empty-state handling on API 37 emulator. | [Parking feature branch](https://github.com/ACZU7/Mobile-Application/tree/feature/parking-finder); PR pending. [Map test record and screenshots 06–10](docs/evidence/parking/README.md#static-map-redesign--2026-10-24) | Pending |
| 2026-10-24 | Omar Abdelatty | Phase 1 Parking Finder: fictional sample `CarPark` model, eight in-memory locations, type/area filters, graph-scoped `ParkingViewModel`, list/details screens and Directions fallback; integrated Parking navigation. Added 12 Parking unit tests. All 46 unit tests and debug build passed. T03 exercised on the running API 37 emulator via ADB; five screenshots retained. | Branch `feature/parking-finder`, based on Housing branch by agreement; implementation recorded on this feature branch, no PR created. [Test and screenshot evidence](docs/evidence/parking/README.md). PR reference pending. | Pending |
| 2026-10-31 | Mohammad AlAbwah | Phase 2 Clean Air Zone checker (tasks 1–7): `CazCheckResult` model, prototype `CazRulesEngine` and six fictional sample vehicles with make and model; `RegistrationValidator` for empty/invalid input; `CleanAirZoneViewModel`; checker screen matching mockup screen 8 (bridge illustration, yellow plate with GB strip, result card for no charge / charge / unknown, vehicle details, prominent prototype statement, "Use the official checker" button with a no-browser fallback). Also hid the app-wide top bar on the housing screens. Unit tests for the rules, validator, repository and ViewModel (including the official-checker fallback); all 99 unit tests and the debug build pass. T08 passed. | Branch `feature/clean-air-zone`, PR pending. Day 1 (2026-10-28): `ef90b15`. Day 2 (2026-10-29): `ccd1466`, `0eb82a5`, `5641f74`, `6bb295d`, `6bfe967`. Day 3 (2026-10-31): `b4b38c0`, `b1ba7ba`, `5404385`, `f6a940a`, `d727c13`, `bc4d30c`, `7469243`. Evidence: T08 in `docs/test-plan.md` (screenshots to be added) |  |
| 2026-10-22 | Mohammad AlAbwah | Phase 1 housing repair slice: `RepairRequest` model and housing enums; in-memory `RepairRepository` with reference generator; `HousingViewModel` with form validation; repair form, confirmation and progress screens; housing navigation graph; restyle to the approved UI design (theme tokens, shared components, 4-step wizard). Unit tests for repository, validator, ViewModel and progress steps. T06 and T07 passed. | Commits `7146680`, `75dcc84`, `7beee67`, `d6c903c`, `a78f618`, `dd23141`, `6514d84`, `4a4bc79`; evidence in `docs/evidence/housing/` |  |

## Environment record

| Item | Value |
|---|---|
| Android Studio version | To be recorded (Android Studio > About Android Studio) |
| Android Gradle Plugin | 9.0.1 |
| Kotlin version | 2.0.21 |
| Compose BOM/version | 2024.09.00 (Material 3, Navigation Compose 2.9.5) |
| Compile SDK | 36 |
| Minimum SDK | 26 (Android 8.0) |
| Target SDK | 36 |
| Primary emulator/device | Medium Phone API 36.1 (Android Studio emulator) |
| Secondary test device/emulator | To be recorded (tablet emulator or physical phone for T09) |

## Omar Abdelatty ? Phase 2 Street Issues, 2026-10-30

Implemented on `feature/street-issue-report` from main `1315c4b`: fictional StreetReport model/enums, in-memory repository and unique references, StateFlow ViewModel and validator, coral category selection, description counter/errors, photo placeholder, static sample map with editable label, local confirmation/disclaimer, My Reports filters/empty state and status timeline. Integrated the nested Streets graph while retaining bottom navigation; Housing and Clean Air Zone files are unchanged.

Actual verification: `./gradlew.bat test` BUILD SUCCESSFUL (67 tests, zero failures/errors; 15 new Street Issues tests); `./gradlew.bat assembleDebug` BUILD SUCCESSFUL. Evidence location: [docs/evidence/streetissues/](docs/evidence/streetissues/README.md). ADB found no connected emulator/device, so manual UI acceptance, screenshots, navigation checks and accessibility/device-size checks remain pending. Peer review pending. No PR created automatically.

## Omar Abdelatty ? Phase 2 emulator verification follow-up, 2026-10-30

On `feature/street-issue-report`, completed T04/T05 local reporting checks and FR05 status/filter/empty-state checks using the API 37 phone emulator through ADB. Verified edited sample location, references SR-2026-0004/SR-2026-0005, disclaimers, Back, Report another issue, tab restoration and Home/Parking/Housing navigation smoke checks. Checked Streets at 150% font scale, landscape, dark mode, actual forced RTL, and a simulated tablet display (1600 x 2560 / 240 dpi on the same API 37 emulator), including draft retention on rotation. The separate API 29 tablet AVD stayed offline; no independent tablet/OS result is claimed.

Fixed two issues found visually: disclaimer punctuation and My Reports selected filters (existing teal theme tokens). Final `./gradlew.bat test assembleDebug`: BUILD SUCCESSFUL in 3s; 67 unit tests, zero failures/errors. Captured 31 unedited screenshots and updated [evidence and verification record](docs/evidence/streetissues/README.md). This supersedes the earlier pending-manual note for the checks above. Human peer review remains pending; Housing and Clean Air Zone files were preserved.

Handover: [PR #3 - Implement Phase 2 street issue reporting flow](https://github.com/ACZU7/Mobile-Application/pull/3), commits `faf9614` and `f748146`. Open for human peer review; no merge or approval performed.

## Omar Abdelatty — Phase 3 directions and location sorting, 2026-11-09

On `feature/parking-directions-location`, branched from `main` at `9b78c5f`:

- **Directions.** Replaced the Phase 1 directions fallback with a confirm-then-open Google Maps intent. If Maps isn't available, it tries a `geo:` link, then a browser. If none can open, a "Directions unavailable" message is shown.
- **Location sorting.** Added on-request location permission (coarse or fine) and a one-off position read through `LocationManager`. The list sorts by haversine distance, and the order survives filter changes.
- **Failure states.** Added explanations for permission denied and location unavailable.
- **Tests.** Added 10 Parking unit tests.

Two bugs were found on the emulator and fixed: a stale cached position, and the list not scrolling to the nearest result after re-sorting.

Verification: `./gradlew.bat test assembleDebug` BUILD SUCCESSFUL, with 126 tests and zero failures. T02 and the T03 directions check passed on the API 37 emulator. Evidence: [parking screenshots 11–17](docs/evidence/parking/README.md#phase-3-directions-and-location-sorting--2026-11-09). Only Parking files, `parking_strings.xml` and the manifest location permissions were changed; Housing, Clean Air Zone, Streets and the shared theme are unchanged. Peer review is pending.
