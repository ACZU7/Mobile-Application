# Omar final handover verification

Date: 25 November 2026. This records coding-agent-operated checks, not human peer approval. The final feature branch remains separate from `main` for review.

Remote refresh confirmed Mohammad's PR #6 merged at `f875d1e`. Merge `1455b6e` brings that main into the handover branch. Three dependency/manifest conflicts were resolved using main's equivalent declaration ordering and comments; no dependency versions or Housing/CAZ feature code changed. Post-integration build/unit/lint verification is recorded with the PR checks.

Post-integration `test assembleDebug assembleRelease lintDebug` passed locally. Remote [Actions run](https://github.com/ACZU7/Mobile-Application/actions/runs/45659929990) failed before either job executed a step because of a GitHub account-side runner restriction. No remote CI pass is claimed; the repository owner must resolve that restriction before rerunning the workflow.

## Ownership

Omar owns Admin/Staff, Parking and Streets. Mohammad owns Home, Profile, Housing and CAZ. The agent-created Home/Profile implementation has been removed at Omar's request. The original Home placeholder is restored and Profile is not implemented. Admin has its own navigation tab. The checks below cover the combined build.

## Completed scope

- The Admin tab opens the Staff demonstration from mockup 11. Reports have status/category filters, a live filtered count, photos/details and saved status updates. Map displays filtered illustrative locations; Analytics counts the same filtered reports.
- Staff Parking edits the eight existing fictional car parks' spaces, hourly price, EV flag and category. Invalid input is rejected. Preferences are saved off the UI thread before the shared flow updates the resident finder/details. A failed save retains the editing dialog and values.
- Staff report status updates save through Room before publishing the shared report flow. The resident status/list reads that same data. No database schema change is needed for the existing status column.
- Fresh gallery verification found that the private photo copy succeeded but navigation after the asynchronous copy could run off the main thread. Launching the gallery callback coroutine explicitly on `Dispatchers.Main.immediate` fixed the return to the form; attachment/removal passed on retest.
- GitHub Actions now defines a build/unit/lint job and API 35 emulator job. A workflow definition is not evidence that remote CI has passed; consult the PR checks.

The current records below supersede the earlier Home/Profile completion and test claims. Those screens and their added test/screenshots have been removed.

## Actual checks

| Check | Result and evidence |
|---|---|
| Build/unit/lint | `gradlew.bat test assembleDebug assembleRelease lintDebug connectedDebugAndroidTest '-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true'` succeeded. 197 JVM unit tests, zero failures/errors. Lint: 52 warnings, zero errors. Debug/release APKs build. |
| Default device suite | Retained XML contains 15 test cases: 9 passed, 6 setup-dependent cases skipped, zero failures/errors. The Gradle console's progress totals differ from the actual XML; this record uses the XML. See [core results](core-results.xml). |
| Staff/resident integration | Changed a report status, checked the repository/resident list, exercised map/analytics, rejected invalid parking input, saved spaces/price and checked resident finder data. Tests restore the original report status and parking sample. [Staff](02-staff-reports.png), [updated status](03-status-updated.png), [map](04-staff-map.png), [analytics](05-staff-analytics.png), [parking edit](06-parking-updated.png), [resident report](07-resident-status.png). |
| Persistence/error boundaries | Unit tests verify failed status writes do not publish a change; missing references are rejected. A real Room close/reopen retains the updated status and the existing migration test preserves Housing. Reopening the parking preferences restores edited spaces/price. |
| External flows | Separately configured `ExternalFlowUiTest` with `externalFlows=true`: 2 passed, zero failures. Used the real system picker, selected a locally generated test image, confirmed the selection, verified attachment/removal and deleted the media fixture. Opened Google Maps with sample coordinates. A rejecting Context wrapper verified that all three handlers are attempted and `false` is returned when none exists; this is a simulated no-handler check, not removal of installed apps. [External results](external-results.xml), [picker](08-system-gallery.png), [attached](09-gallery-attached.png), [Maps](10-directions-hand-off.png). |
| Responsive Admin | The Admin integration test passed at 1600 × 2560 / 240 dpi, rotated to actual 2560 × 1600 landscape, 150% font and dark mode. This is the same API 37 emulator, not a second physical device. The retained captures are LTR; no new RTL result is claimed. [Results](responsive/results.xml), [staff](responsive/02-staff-reports.png), [parking](responsive/06-parking-updated.png). |
| Documents | Mini report rendered to three pages and design review copy to 16 pages; all pages visually inspected. The source design draft is preserved. Generated documents/ZIP remain local because they contain submission identifiers and need group review/signatures. |

Primary emulator: `Medium_Phone_API_37.0`, API 37, x86_64. Normal display: 1080 × 2400 / 420 dpi. IDE product information reports Android Studio `AI-262.9437.185.2621.16467767` (2026.2.1); bundled JDK OpenJDK `25.0.3`, build `25.0.3+-15898627-b508.16`. Minimum/compile/target SDK: 26/36/36; Gradle wrapper 9.1.0 and AGP 9.0.1.

## Running the external checks

Run only after the core suite completes, on an emulator with a working system photo picker:

```powershell
.\gradlew.bat connectedDebugAndroidTest '-Pandroid.testInstrumentationRunnerArguments.class=com.example.mobileapplication.ExternalFlowUiTest' '-Pandroid.testInstrumentationRunnerArguments.externalFlows=true' '-Pandroid.injected.androidTest.leaveApksInstalledAfterRun=true'
```

Permission/reboot fixtures and their previous results remain in [Phase 4 evidence](../phase4/README.md). Default skips must not be counted as passes. Natural future reminder delivery, physical camera flash, a physical device and TalkBack have not been newly verified. English fallback plus RTL layout evidence does not establish Arabic translation.

## Handover boundary

Human peer review and merge, the students' own video/explanation, both signatures, Group ID and Mohammad's student number remain necessary. The existing Housing photo gap is Mohammad-owned and is explicitly recorded rather than implemented or claimed here. The public repository excludes generated assessment documents and the ZIP. See [submission instructions](../../submission/README.md) and [Omar's timed walkthrough](../../submission/omar-walkthrough.md).
