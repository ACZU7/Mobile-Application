# Technical decisions

## Agreed stack

| Area | Decision | Reason |
|---|---|---|
| Platform | Native Android in Kotlin | Required by the assessment. |
| UI | Jetpack Compose and Material 3 | Supports the module learning and the agreed UI design. |
| Architecture | Feature-based MVVM | Keeps Omar Abdelatty and Mohammad AlAbwah's code independent, readable and assessable. |
| Navigation | Navigation Compose | Provides predictable navigation and back-stack behaviour. |
| Data before APIs | Curated sample data and repositories | Lets the group complete working flows without depending on unstable/unauthorised integrations. |
| Local persistence | Room in quality-core phase | Suitable for favourites, local reports, repairs, permits and sample rent history. **Change (Phase 3, Omar):** the single sample resident's permit and its reminder setting use Preferences DataStore (`resident_permit`) instead of Room. It holds one record of five values, which doesn't need a database. This keeps Parking independent of the Room setup Mohammad is adding for Housing, and avoids a second database. To be confirmed with Mohammad. |
| Directions | Google Maps intent | Gives practical directions without building a navigation engine. |
| Camera | Android photo picker/camera flow | Supports photo evidence while allowing a clear no-photo path where appropriate. Implemented (Phase 3, Omar): an in-app CameraX camera matching mockup 5, the system photo picker (no storage permission), and Coil for thumbnails. `CAMERA` is requested only after the user taps Add photo. If it is denied, the gallery and "Continue without photo" are still offered. Photos are copied into app-private storage and never uploaded. |
| Location | Android location services | Supports distance ordering and editable report pins; manual fallback is required. Implemented (Phase 3) with the framework `LocationManager`, so no Google Play Services dependency was added. It accepts approximate location and asks only when the user taps Find my location. |
| Notifications | WorkManager/local notifications | Used only after core flows work, for permit/rent reminders. Implemented for the permit (Phase 3): a unique one-time WorkManager job, 14 days before expiry at 9am, so it survives restarts and reboots. `POST_NOTIFICATIONS` is requested only when the reminder switch is turned on. The rent reminder can follow the same pattern. |
| Authentication | Local demonstration profile first | Firebase Auth is not a dependency for the MVP and is only a stretch integration. |

## Proposed package layout

```text
app/
  data/
    model/
    repository/
  ui/
    shared/
    parking/        # Omar Abdelatty
    streetissues/   # Omar Abdelatty
    housing/        # Mohammad AlAbwah
    cleanairzone/   # Mohammad AlAbwah
  navigation/
```

## Guardrails

- No live payments or real Council-account integration.
- No claim that a sample car-park result, report status, rent balance or CAZ answer is official/live data.
- Do not add Firebase, maps, camera, location or notifications until the shared navigation shell and the basic local feature flow work.

