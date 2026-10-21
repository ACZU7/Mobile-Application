# Implementation plan

This is the working roadmap for the Bristol City Companion implementation phase. The priority is a stable, testable native Android pilot using clearly labelled local/sample data. Complete the MVP before quality-core or stretch features.

## Working rules

- Build with Kotlin, Jetpack Compose, Material 3, Navigation Compose and a feature-based MVVM structure.
- Work on a separate branch for each GitHub Issue. Open a pull request, review it together, then merge to `main`.
- Keep each commit focused and descriptive, for example: `Add parking filter chips`.
- Update `contributions.md` with completed work and evidence. Add a meeting note when you make a new decision or change scope.
- Do not claim the pilot submits real Bristol City Council reports, receives payments, accesses Council accounts/rent data, or returns an official CAZ decision.
- Add camera, GPS, maps, notifications, Room, Firebase and live APIs only after the corresponding local flow is working without them.

## Definition of done

A task is complete only when:

1. The feature works on the selected emulator/device without crashing.
2. Normal, invalid/empty and permission-denied states are handled where relevant.
3. The UI follows the agreed design system and has readable labels.
4. The code is committed on the owner's branch and reviewed by the other member.
5. The related test case is run and evidence is retained.
6. The contribution log and, where needed, meeting notes are updated.

## Phase 0 Set up the shared project

### Omar Abdelatty

- Create the Android Studio **Empty Activity** project: Kotlin, Jetpack Compose, minimum SDK agreed by both members (API 26 or above).
- Push the generated Gradle project to `main`.
- Create the feature package skeleton:

```text
app/
  data/
    model/
    repository/
  ui/
    shared/
    parking/
    streetissues/
    housing/
    cleanairzone/
  navigation/
```

- Add Parking and Streets placeholder destinations.

### Mohammad AlAbwah

- Set up the Material 3 colour, typography and spacing choices from the approved UI design.
- Create reusable components such as `AppTopBar`, `PrimaryButton`, status chip and service card.
- Add Housing and Clean Air Zone placeholder destinations.
- Create initial sample model classes shared by the application, for example `CarPark`, `StreetReport`, `RepairRequest` and `CazCheckResult`.

### Both

- Create the dashboard and bottom navigation: Home, Parking, Streets and Housing.
- Agree route names before feature development begins.
- Confirm the app builds from a clean clone on both machines.
- Create GitHub Issues for every task below, assigning each to its owner.

### Completion check

- The app opens to Home; all four destinations open; Android Back behaves sensibly; there are no crashes.
- Evidence: screenshot/video of navigation and a completed T01 test entry.

## Phase 1 First vertical slices

The aim is a complete, simple user journey for one Omar feature and one Mohammad feature. Use fixed sample data. Do not add device permissions yet.

### Omar Abdelatty Parking finder

1. Create a `CarPark` model: name, type, area, address, opening information, price note, spaces note, latitude and longitude.
2. Add a local repository with at least 68 clearly labelled sample car parks/Park & Ride locations.
3. Build the Parking list screen with a title, short prototype-data notice, cards and empty-state handling.
4. Add filter chips for parking type/area and ensure changing a filter updates the list.
5. Build a Parking Details screen with the selected car parks information.
6. Add a Directions button. Initially show a safe fallback message; later replace this with a Google Maps intent.
7. Keep state in a `ParkingViewModel`, not inside large composables.

### Mohammad AlAbwah Housing repair report

1. Create a `RepairRequest` model: reference, room, issue type, description, appointment preference, created date, status and optional image URI.
2. Create a local repository with sample repair-progress data.
3. Build the repair form: room picker, problem picker, description input and appointment preference.
4. Validate required fields and explain errors next to the field or clearly at the top of the form.
5. On submission, create a local request with a visible generated reference.
6. Build a confirmation screen and a Repair Progress screen showing the saved request and a clearly labelled demonstration status.
7. Keep state in a `HousingViewModel`.

### Both

- Make the two flows visually consistent and reachable from the dashboard.
- Review each other pull requests before merging.
- Test T02 T07 that apply to completed work; retain screenshots.

### Completion check

- A user can browse/filter/select sample parking and complete a housing-repair request from start to finish.
- Evidence: one short screen recording per flow, pull requests, commits and contribution-log entries.

## Phase 2 Complete the service MVP

### Omar Abdelatty Street issue reporting

1. Create a `StreetReport` model: reference, category, description, location label/coordinates, image URI, created date and status.
2. Build the report form with category selection and description input.
3. Add validation for missing category and description.
4. Provide an editable location field or a sample map-pin location. It must be clear that the initial report is stored locally.
5. Generate a reference number and confirmation screen after a valid submit.
6. Create My Reports/status screen, listing submitted local reports with reference, category, date and visible demonstration status.
7. Add an optional photo attachment only after the form/confirmation/status flow works.

### Mohammad AlAbwah Clean Air Zone checker

1. Create a local `CazCheckResult` model and simple rules engine appropriate for a demonstration.
2. Build a registration input screen with clear format guidance.
3. Validate empty and clearly invalid entries; do not try to identify a real vehicle from the registration.
4. Build a result state showing an example prototype outcome.
5. Include a prominent statement that it is a **prototype/sample result, not an official charge decision**.
6. Add an Official Checker action which opens the Council/Government checker website in the browser where possible, with a fallback if no browser exists.
7. Keep rules and UI separate so the rules can be improved without editing composables.

### Both

- Add common loading, empty, success and error patterns.
- Review labels and language for clarity and accessibility.
- Test the completed requirements and record results in the test plan/evidence log.

### Completion check

- A user can submit and review a local street report; a user can check a registration-format input and see a clearly non-official CAZ prototype result.
- Evidence: T04, T05 and T08 tests complete, plus screenshots of confirmation and invalid-input states.

## Phase 3 Device capabilities and quality-core work

Complete these only after Phases 02 have a stable build.

### Omar Abdelatty Permissions, directions and parking permit

1. Replace the Directions fallback with a Google Maps intent using the selected car parks coordinates. Keep a user-friendly fallback if Maps is unavailable.
2. Request location only when the user asks to sort/find nearby parking. If denied, show the list without distance sorting and explain the alternative.
3. Add a Street Issue photo-picker/camera flow if time allows. It must be optional and remain usable if permission is denied.
4. Build a resident parking-permit screen using sample permit data: permit zone/reference and expiry date.
5. Store the sample permit locally and schedule a local expiry reminder only after notification permission is granted.

### Mohammad AlAbwah Rent account and persistence

1. Build a rent-account screen using clearly labelled sample balance and payment-history data.
2. Add a local reminder choice; request notification permission only when the user enables it.
3. Introduce Room/local persistence for repair requests, saved settings and the relevant sample data.
4. Verify saved data remains after app restart.
5. Keep all rent information visibly labelled as sample/demo data.

### Both

- Agree one local-persistence approach and avoid duplicate databases/repositories.
- Test permission allowed/denied flows and persistence after an app restart.
- Complete T09 and T10; update technical decisions if implementation changes the agreed design.

### Completion check

- Location/camera/notification permission decisions do not block core flows; relevant local data survives restart.
- Evidence: permission-denied screenshots, persistence recording, T09/T10 results.

## Phase 4 Polish and final verification

### Omar Abdelatty

- Test Parking and Streets features against FR01 FR05 and QR01.
- Fix layout, validation, navigation and permission issues in owned features.
- Capture final screenshots/video clips showing main and error states.

### Mohammad AlAbwah

- Test Housing and CAZ features against FR06 FR08 and QR02.
- Fix layout, validation, persistence and wording issues in owned features.
- Capture final screenshots/video clips showing main and error states.

### Both

- Run every test in `docs/test-plan.md` on the primary emulator and a second screen size/device if available.
- Test larger system font size, colour-independent status messages and tappable controls.
- Review all prototype disclaimers and remove any inaccurate claim of live/official data.
- Resolve outstanding GitHub Issues; update `contributions.md` with commits, PRs, testing and review evidence.
- Prepare the assessment video. Each person explains their own implementation decisions and code.

### Completion check

- All applicable tests pass or have a recorded issue/fix/retest outcome.
- The project builds from `main`, the contribution record is complete, and both members can explain their individual work.

## Optional stretch backlog

Only start these when every MVP and quality-core task above is complete and tested.

| Owner | Stretch task | Minimum safe scope |
|---|---|---|
| Omar Abdelatty | Parking timer | User chooses a sample expiry time and receives a local reminder ten minutes before it. |
| Omar Abdelatty | Parking fine lookup/appeal | Demonstration records only; no real fine payment or live appeal submission. |
| Mohammad AlAbwah | CAZ geofence alert | Demonstration geofence with clear permission and battery-use explanation. |
| Mohammad AlAbwah | Housing help checker | Short decision tree that directs the user to a service category; no automated eligibility decision. |
| Both | Firebase authentication/profile | Only if it does not destabilise the local MVP; never required to demonstrate core workflows. |

## Suggested GitHub Issue titles

Create one Issue per item, using labels such as `omar`, `mohammad`, `shared`, `mvp`, `quality-core`, `testing` and `stretch`.

```text
Set up Kotlin Compose project and feature package structure
Create shared dashboard and bottom navigation
Add shared Material 3 theme and reusable components
Build Parking sample-data list and filters
Build Parking details and Maps directions fallback
Build Housing repair form and validation
Build Housing repair confirmation and progress screens
Build Street Issue form and local report status
Build Clean Air Zone prototype checker
Add local persistence for reports, repairs and settings
Add parking-permit expiry reminder
Add sample rent account and reminder
Run accessibility, device-size and permission tests
Prepare final evidence and video walkthrough
```

## Weekly routine

1. Meet briefly; update `docs/meetings/` with decisions and tasks.
2. Select a small number of Issues for the week and assign an owner/due date.
3. Develop on branches, committing meaningful increments.
4. Open pull requests; the other person reviews before merging.
5. Test completed work, add evidence and update `contributions.md`.
6. At the end of the week, check this roadmap and move unfinished work forward explicitly rather than silently dropping it.
