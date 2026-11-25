# PR 8 merge conflict verification

Combined `feature/home-dashboard-profile` at `1d89d12` with `main` at `11f10c2`. This is a conflict-resolution merge into the PR branch, not a merge of PR #8 into main and not a claim of human peer approval.

## Resolution

- Resolved `AppDestination.kt` using Mohammad's resource-based tab definitions and nested-route selection, preserving Clean Air Zone highlighting under Parking. Retained the Staff destination, with Profile selected while Staff is open. The tab list uses a getter to avoid singleton initialization cycles.
- Resolved `BristolCityCompanionApp.kt` using Mohammad's Home/Profile graphs, service routing, tab restoration and status-bar behaviour. Preserved Omar's existing Staff route and ViewModel/repositories.
- Added the existing **Admin dashboard** entry to the full Profile screen through a navigation callback and the existing shared secondary button. Profile editing, saving, clearing and existing layout remain Mohammad's implementation.
- Removed duplicate navigation string definitions introduced by merging the two resource files. Existing integration tests now use the real Home CAZ tile and wait for Profile data before finding the Admin entry.

## Checks

- `gradlew.bat test assembleDebug assembleRelease lintDebug`: successful. 241 JVM tests, zero failures/errors; debug/release builds pass. Lint has warnings but no errors.
- Focused API 37 emulator suite: **4 passed, zero skipped/failures**. Includes all six Home service destinations, Profile editing/Cancel, Admin entry and Back to Profile, Profile tab selection while in Staff, report-status and parking-edit integration, Housing repair/progress and CAZ prototype results. See [results](results.xml).
- No unresolved Git entries, conflict markers or duplicate named resources remain.

Screenshots: [Home](01-home.png), [Profile with Admin](02-profile-admin.png), [Staff](03-staff.png). These describe the combined PR build; earlier main-branch statements that Home/Profile were placeholders are superseded for this PR only.
