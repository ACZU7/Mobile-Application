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
