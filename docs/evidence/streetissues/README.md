# Street Issues Phase 2 evidence

Owner: Omar Abdelatty. Branch: `feature/street-issue-report`.
Verified on 2026-10-30 by the coding agent operating the emulator through ADB.

## Automated verification

- `./gradlew.bat test`: 67 tests, zero failures/errors, including 15 Street Issues tests. Existing Housing and Parking tests passed.
- `./gradlew.bat assembleDebug`: BUILD SUCCESSFUL. Final combined `./gradlew.bat test assembleDebug` run: BUILD SUCCESSFUL in 3s, 41 tasks (7 executed, 34 up-to-date).
- Final APK installed from `app/build/outputs/apk/debug/app-debug.apk`.
- `git diff --check`: passed.

Tests cover required category/description, trimmed minimum 10 and maximum 500 characters, blank location, reference format/sequence, repository save/retrieve, valid and invalid ViewModel submission, correction after validation, status filtering and empty results.

## Manual emulator results

Primary: Medium Phone API 37, 1080 x 2400, 420 dpi, approximately 411 dp wide. All checks below were actually exercised; no physical-device run is claimed.

| Check | Result | Evidence |
|---|---|---|
| T05 empty submission | Passed: readable category and description errors; no navigation/submission. | [Description error](02-invalid-empty-description.png), [category error](03-invalid-category.png) |
| T05 length limits and recovery | Passed: 5-character and 501-character descriptions rejected; correcting the description allowed submission. | [Short](05-short-description.png), [overlong](06-overlong-description.png), [valid form](07-valid-form.png) |
| T04 category and location | Passed: selected category is coral; edited location to fictional Demo Test Lane, Bristol; initial submission retained it. | [Location editor](04-edit-location.png); later final-build rerun used the default sample label. |
| T04 local submission/reference | Passed: final build saved SR-2026-0004 with matching category/description/location; explicit local-only disclaimer visible. | [Confirmation](08-confirmation.png), [status](09-submitted-status.png) |
| FR05 reports and filters | Passed: All showed saved/sample reports; Submitted showed only submitted reports; In progress showed SR-2026-0003; Fixed showed the empty state. | [All](10-my-reports-all.png), [Submitted](11-filter-submitted.png), [In progress](12-filter-in-progress.png), [empty](13-filter-empty.png) |
| Status timeline/detail | Passed: tapping SR-2026-0003 opened its matching status with Submitted done, In progress current and Fixed upcoming. Steps expose numbered accessibility descriptions. | [Detail](14-in-progress-detail.png) |
| Android Back and new report | Passed: status Back restored the list/filter; Back from submitted status restored confirmation; Report another issue returned to a reset form; tab switching restored Streets and its filter. Streets remained selected throughout. | [Restored filter](15-back-restores-filter.png), [reset form](01-form.png) |
| T01 regression smoke check | Passed: Home, Parking finder and Housing form opened; returning to Streets restored its nested destination. This is a navigation smoke check, not a full repeat of Housing/Parking acceptance. | [Parking](16-parking-regression.png), [Housing](17-housing-regression.png), [Home](18-home-regression.png) |
| T09 large font | Passed at 150% font scale: report labels/status/timeline readable, form scrolls and Submit report remains reachable. | [Reports](19-large-font-reports.png), [form top](20-large-font-form-top.png), [form bottom](21-large-font-form-bottom.png) |
| Landscape | Passed: content constrained/scrollable; description, counter, disclaimer and Submit report reachable above bottom navigation. | [Form top](22-landscape-form.png), [form bottom](23-landscape-form-bottom.png) |
| Dark mode | Passed: shared dark theme applies, labels/actions remain readable. | [Dark form](24-dark-form.png) |
| RTL | Passed using actual Android Force RTL configuration (`am get-config` reported `ldrtl`): top-bar arrow, nav, categories, cards and timeline mirrored. English fallback labels are retained; Arabic translation was not added. | [RTL form](26-rtl-form.png), [RTL reports](27-rtl-reports.png) |
| T09 simulated second size | Passed at 1600 x 2560 / 240 dpi (`sw1067dp`, xlarge) on the same API 37 emulator. Draft category/description survived landscape rotation; submitted SR-2026-0005 and opened status/reports. | [Portrait form](28-tablet-size-form.png), [rotated draft](29-tablet-landscape-draft.png), [confirmation](30-tablet-confirmation.png), [status](31-tablet-status.png), [reports](32-tablet-reports.png) |

The separate installed API 29 tablet AVD stayed offline and could not be tested. It was stopped; the second-size result above is a simulated tablet configuration, not a second device/OS. Phone size, density, orientation, font scale, night mode and RTL were restored after testing. No app crash was observed; the device crash buffer contained unrelated Bluetooth hardware-error crashes.

## Issues found and retested

1. Prototype disclaimer contained a literal question mark in place of punctuation. Corrected the string resource and verified the final confirmation/status/form screenshots.
2. My Reports selected filters inherited Material's lavender container. Set them explicitly to existing theme teal/onPrimary and neutral tokens; verified filters and 150% font screenshots after rebuilding.

Screenshots 02-06 and 20-21 retain the initial validation/layout run before these cosmetic fixes, so their disclaimer may show the old punctuation. Final captures 07-19 and 22-32 (25 intentionally unused) show the corrected build. No images were edited.

## Scope and remaining review

- All reports/statuses/locations are fictional demo data. Records are in memory, lost on process exit, and never submitted to Bristol City Council.
- Static illustration only; editing the label retains illustrative coordinates. No geocoding, camera/gallery, GPS, maps SDK, permissions, network integration or persistence.
- Fixed is a supported status; no initial Fixed records are seeded so its empty state is demonstrable.
- Shared theme, top bar, buttons, status chips and progress stepper are reused. Filters use two rows for readable labels. Camera mockup 5 remains deferred; its categories are integrated into the details form.
- Human peer review by Mohammad remains outstanding. No TalkBack session, Arabic translation, physical-device run or broad accessibility certification is claimed.

Handover: [PR #3](https://github.com/ACZU7/Mobile-Application/pull/3); implementation `faf9614`, emulator evidence/fixes `f748146`.
