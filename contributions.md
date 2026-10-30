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
