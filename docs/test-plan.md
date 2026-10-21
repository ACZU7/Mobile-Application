# MVP test plan

## Test approach

Test every acceptance criterion manually on the primary emulator. Repeat key tests on a second Android screen size and, if available, a physical Android device. Keep screenshots or short screen recordings as evidence.

## Core test cases

| ID | Test | Expected result | Owner |
|---|---|---|---|
| T01 | Launch the app and use every navigation destination. | All destinations open; back navigation is sensible; no crash. | A + B |
| T02 | Open Parking with location allowed and denied. | Results sort by distance when allowed; an understandable fallback appears when denied. | A |
| T03 | Apply parking filters and open a car park. | Filtered list updates; details show; directions hand-off works or provides a fallback. | A |
| T04 | Submit a valid street report. | Category, text, optional photo and editable location are captured; confirmation/reference/status appear. | A |
| T05 | Submit an incomplete street report. | Required-field validation clearly explains what must be corrected. | A |
| T06 | Submit a valid housing repair. | The repair is confirmed with selected room, problem and appointment preference. | B |
| T07 | Open housing repair progress. | Saved repair data and demonstration status are shown. | B |
| T08 | Enter valid and invalid CAZ registrations. | Validation is clear; every result states that it is a prototype result and provides an official-checker action. | B |
| T09 | Test large font size and a second screen size. | Core labels/actions remain visible, usable and not clipped. | A + B |
| T10 | Deny camera/notification permissions. | The affected flow remains usable and gives an appropriate explanation. | A + B |

## Evidence to retain

- Screenshots/recordings for each completed test.
- Issues found, owner, fix and retest outcome.
- Commit links for the feature that was tested.

