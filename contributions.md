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
