# Meeting 02 agenda (to confirm)

Proposed agenda for the next meeting. After the meeting, rename this file to `YYYY-MM-DD-meeting-02.md`, record what was actually decided, and fill in the actions table.

**Attendees:** Omar Abdelatty, Mohammad AlAbwah

## To discuss

1. **Housing pull request** (`feature/housing-repair-report`): review and merge. It changes two lines in `BristolCityCompanionApp.kt` (Housing tab opens the repair wizard; `consumeWindowInsets` on the content area) and lowers core-ktx/lifecycle so the project builds with SDK 36.
2. **Duplicate top bars:** the shell's top bar sits above each screen's own top bar from the mockups. Proposal: remove the shell top bar and use the shared `TopBar` on every screen.
3. **Hard-coded text in the shell:** move the app title and placeholder text into `strings.xml`, as the plan requires.
4. **Package name:** rename `com.example.mobileapplication` (e.g. `uk.ac.uwe.bristolcompanion`) now, before more code is written. One person does it in a single PR while the other has nothing open.
5. **UI design spec:** agree that `docs/design/ui-mockups.png` and `docs/design/ui-spec.md` are the reference for all screens, and that Parking and Streets reuse the components in `ui/shared`.
6. **Scope for higher marks:** the rubric's top bands (complexity 20%) ask for advanced features and multiple external APIs/services. Choose two or three to add to Phase 3, for example:
   - live Bristol open data for car parks and parking zones, cached for offline use
   - an in-app map (Maps Compose) with location-based sorting
   - Firebase Authentication and a staff view that updates report/repair status
   - Arabic/Polish translations with a right-to-left layout
   - the DVSA MOT History API for the CAZ checker (vehicle details, with the prototype disclaimer kept)
7. **Commit messages and contributions log:** each commit message should say what changed, and each member records their work in `contributions.md`, so individual marking is clear.
8. **GitHub Issues:** create the issues listed at the end of `docs/implementation-plan.md` and assign owners.
9. **Design document (Stage 2, 35%):** agree an outline and split the sections (requirements and personas, competitor review, wireframes, composites, licensing).

## Decisions

- To be recorded at the meeting.

## Actions

| Owner | Task | Due date | Status |
|---|---|---|---|
| Omar Abdelatty |  |  | Not started |
| Mohammad AlAbwah |  |  | Not started |
| Both |  |  | Not started |

## Next meeting

- Date/time:
