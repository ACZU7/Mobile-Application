# Phase 4 verification — 22 November 2026

Branch: `feature/phase-4-verification`, based on `main` at `1887c29`. Mohammad's `feature/rent-account-persistence` was combined locally in merge `7f21003`; GitHub main has not been changed by this verification work. This record describes agent-operated tests on the Android emulator, not human peer approval.

## Changes verified

- Street Reports now use the same Room database as Housing. Version 2 adds `street_reports`; migration 1 to 2 preserves the existing repairs table. Report references, categories, descriptions, location labels/coordinates, dates, statuses and private photo URI lists are saved.
- Confirmation follows a successful database write. A failed write preserves the form/photos, shows an error and does not publish a successful submission. Database work is asynchronous; confirmation navigation runs on the main thread.
- Short landscape displays place the Parking/permit map and results in adjacent panels, mirrored in RTL. Portrait retains the existing map and bottom-sheet arrangement.
- Updated the Street Report disclaimer to describe on-device persistence. Permit map drawing uses Compose density rather than reading resources through `LocalContext`.

## Automated checks

`./gradlew.bat test assembleDebug assembleRelease lintDebug connectedDebugAndroidTest -Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true`

The combined build has 194 unit tests, zero failures/errors. Tests cover retained fields/photo URIs, reference numbering after reload, concurrent saves and write-failure recovery. Device tests exercise a real database reopen and a version-one migration with an existing Housing repair, as well as UI journeys. See [combined device results](combined-results.xml). Parameter-dependent permission/reboot tests are skipped in the default suite and were run separately with the matching emulator setup; those skips are not counted as passes.

## Device and UI evidence

Primary device: `Medium_Phone_API_37.0`, Android API 37 emulator. Normal display is 1080 × 2400 at 420 dpi. Tablet sizing uses this same emulator at 1600 × 2560 / 240 dpi; it is not a separate device or OS.

| Check | Actual result and retained evidence |
|---|---|
| Parking | EV filtering, selected result/details, fictional-location Directions confirmation and Cancel/Back, permit entry and loaded permit card passed. [Phone finder](phone/01-parking-ev.png), [Directions confirmation](phone/02-directions-confirm.png), [permit](phone/03-permit.png). |
| Streets | Empty submission validation, valid submission, saved confirmation, status and empty Fixed filter passed. [Validation](phone/04-street-validation.png), [saved](phone/05-street-saved.png), [status](phone/06-street-status.png), [empty filter](phone/08-reports-empty.png). |
| Integration | Housing/rent screen remains reachable; new Housing repair submission and progress passed; CAZ empty validation and no-charge/charge/unknown results retained the prototype statement and official-checker action. Housing/CAZ implementation was not rewritten. |
| Large font + dark mode | Core Parking, permit, Streets and rent-navigation journeys passed at 150% font in dark mode. [Permit](dark-large-font/03-permit.png), [Streets](dark-large-font/05-street-saved.png), [rent](dark-large-font/07-rent-integration.png). |
| Second screen size | The same core journeys passed at simulated tablet dimensions. [Finder](tablet-size/01-parking-ev.png), [permit](tablet-size/03-permit.png), [Streets](tablet-size/05-street-saved.png). |
| RTL | Core journeys passed with actual Android `ldrtl` configuration. English fallback text was used; this is not an Arabic translation test. [Permit](rtl/03-permit.png), [Streets](rtl/05-street-saved.png). |
| Landscape + offline | Final adjacent-panel layout passed at 2400 × 1080, with RTL also active and Wi-Fi/mobile data disabled. Lists, details, permit and local report submission remained usable. Cached tiles may appear; no complete offline map download is promised. [Finder](landscape-offline/01-parking-ev.png), [permit](landscape-offline/03-permit.png), [Streets](landscape-offline/05-street-saved.png), [results](landscape-offline/results.xml). |
| Permissions denied | Location denial explained the default ordering; notification denial showed an explanation; camera denial retained Continue without photo. Permissions were fixed-denied using ADB to exercise the app's fallback; this does not replace the Phase 3 system-prompt evidence. [Location](denied-permissions/10-location-denied.png), [notifications](denied-permissions/11-notifications-denied.png), [camera](denied-permissions/12-camera-denied.png). |
| Permissions allowed | Camera capture, switching the emulator camera, attachment and removal passed. Location sorting passed using fictional test-provider coordinates 51.47, -2.52. [Camera](allowed-permissions/13-camera.png), [photo](allowed-permissions/14-photo-attached.png), [sorted parking](allowed-permissions/15-location-sorted.png). |
| Reminder reboot/cancel | Enabled the sample permit reminder; after a real emulator reboot the saved switch remained on and WorkManager still held the scheduled job. Turning it off cancelled the pending job. [Enabled](reminder-reboot/16-reminder-enabled.png), [after reboot](reminder-reboot/17-reminder-after-reboot.png). This is persistence/rescheduling evidence, not proof of natural notification delivery at the future expiry date. |

## Findings and limits

The first device run exposed a main-thread navigation failure after asynchronous Street submission; explicitly dispatching the submit/navigation coroutine to `Dispatchers.Main.immediate` fixed it and the device journey passed on retest. Visual review found that the original short landscape map was hidden behind controls. The final adjacent-panel layout was retested in actual landscape and RTL.

Lint completes with no errors; warnings remain for dependency/target versions, plural resources, unused resources and KTX suggestions. No dependency upgrade was bundled into final verification.

Still outstanding before project sign-off:

- Human review of the combined branch and merge to main, followed by verification of the merged commit.
- The shared Home screen is still a placeholder rather than approved mockup 2. Housing photos are still labelled as unavailable. These existing shared/Mohammad-owned scope gaps need agreement before claiming complete design/FR06 acceptance.
- A fresh gallery-picker/real Maps hand-off and no-handler fallback run was not repeated in this pass; retained Phase 3 evidence remains in the Parking/Streets evidence folders.
- Physical-device flash behavior, a separate Android OS/device, TalkBack navigation and natural future reminder delivery were not verified.
- Both students must record their own assessment explanation and peer-review evidence.

## Assessment walkthrough preparation

Omar: show EV filtering and car-park details; explain sample-data warnings and Directions fallback. Show on-request location and the denied path. Open the permit and explain DataStore for the single permit versus Room for report lists, plus WorkManager and notification permission. Submit an invalid then valid Street Report, show an optional photo, confirmation/reference and My Reports; close/reopen the app to demonstrate persistence. Explain ViewModel state, validation, the Room migration and what the tests establish.

Mohammad: demonstrate the Housing repair wizard, persisted repair/progress, sample rent/history and reminder, then CAZ invalid input and each prototype result. Explain their own repository/ViewModel/database and reminder decisions. End by stating the remaining prototype limits and showing the recorded checks; do not claim real Council submission, payment or official CAZ decisions.
