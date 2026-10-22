# Initial Android project setup

## Current baseline

The repository contains the native Android starter app and a shared Compose navigation shell.

- Language: Kotlin
- UI: Jetpack Compose with Material 3
- Minimum SDK: 26
- Navigation: Navigation Compose
- App label: Bristol City Companion
- Package (temporary): `com.example.mobileapplication`

The existing package is retained for now so both contributors can pull and run the project without a disruptive folder rename. Rename it later only if both members agree.

## Source layout

```text
app/src/main/java/com/example/mobileapplication/
  MainActivity.kt
  ui/
    navigation/
      AppDestination.kt
      BristolCityCompanionApp.kt
    theme/
  data/
    model/              # Shared models
    repository/         # Local/sample data sources
  features/
    parking/            # Omar Abdelatty
    streetissues/       # Omar Abdelatty
    housing/            # Mohammad AlAbwah
    cleanairzone/       # Mohammad AlAbwah
```

Create feature code inside the appropriate `features/` folder. Do not put feature logic in `MainActivity.kt` or `BristolCityCompanionApp.kt`.

## First run

1. Clone the repository and open it in Android Studio.
2. Select **File > Sync Project with Gradle Files**.
3. Use an Android emulator/device with API 26 or above.
4. Run the app. You should see Home plus bottom navigation for Parking, Streets and Housing.
5. If the project does not build, record the full error in a GitHub Issue rather than changing Gradle versions without agreement.

## Starting a feature

1. Choose your assigned GitHub Issue.
2. Create a branch, e.g. `feature/parking-list` or `feature/housing-repair-form`.
3. Implement the feature in its assigned folder using a ViewModel and local/sample data.
4. Run the relevant tests from `docs/test-plan.md`.
5. Open a pull request; the other member reviews it before merge.
6. Update `contributions.md` with the issue, PR/commit and test evidence.

## Shared-code rule

Tell the other member in a meeting note or GitHub Issue before changing navigation, theme, Gradle files or reusable UI components. This keeps integration work clear and reduces merge conflicts.
