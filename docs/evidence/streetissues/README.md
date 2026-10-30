# Street Issues Phase 2 evidence

Owner: Omar Abdelatty. Branch: `feature/street-issue-report`.

## Completed verification ? 2026-10-30

- `./gradlew.bat test`: BUILD SUCCESSFUL in 22s; 67 tests, zero failures/errors, including 15 Street Issues tests. Existing Housing and Parking tests passed.
- `./gradlew.bat assembleDebug`: BUILD SUCCESSFUL in 4s. APK: `app/build/outputs/apk/debug/app-debug.apk`.
- `git diff --check`: passed.
- ADB device check: no connected emulator/device. No screenshots or recordings captured; no manual UI pass is claimed.

Tests cover required category/description, trimmed minimum 10 and maximum 500 characters, blank location, reference format/sequence, repository save/retrieve, valid and invalid ViewModel submission, correction after validation, status filtering and empty results.

## Manual checks still required

Install the debug APK on an emulator/device and retain screenshots here for:

1. Streets form: select a coral category, enter at least 10 characters, edit fictional location label and submit. Check confirmation category, description, location and generated reference.
2. Submit empty, short and over-500-character descriptions; confirm errors are readable and correction allows submission.
3. Open report status and My reports; check All, Submitted, In progress and Fixed filters. Initial Fixed results are intentionally empty; Fixed is a supported demonstration status.
4. Check Submitted ? In progress ? Fixed timeline, tapping reports, Android Back and Streets navigation selection throughout nested routes.
5. Check prototype disclaimer on form, confirmation and status; sample-data notice throughout. Reports must never appear officially submitted.
6. Check Home, Parking and Housing navigation, rotation, landscape, large fonts, dark mode, RTL and a second screen size.

## Scope notes

- Static local illustration only; edited label retains illustrative coordinates, with no geocoding.
- Optional photo placeholder only. No camera/gallery, GPS, maps SDK, permissions, network integration or persistence.
- All reports/statuses/locations are fictional demo data. New records are Submitted; statuses are not live updates. Data is lost on process exit.
- Layout reuses the shared theme, top bar, buttons, status chips and progress stepper. Filters wrap into two rows for readable labels. Photo capture from mockup 5 is deferred; its categories are integrated into the mockup 6 form.
