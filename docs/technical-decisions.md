# Technical decisions

## Agreed stack

| Area | Decision | Reason |
|---|---|---|
| Platform | Native Android in Kotlin | Required by the assessment. |
| UI | Jetpack Compose and Material 3 | Supports the module learning and the agreed UI design. |
| Architecture | Feature-based MVVM | Keeps Owner A and Owner B code independent, readable and assessable. |
| Navigation | Navigation Compose | Provides predictable navigation and back-stack behaviour. |
| Data before APIs | Curated sample data and repositories | Lets the group complete working flows without depending on unstable/unauthorised integrations. |
| Local persistence | Room in quality-core phase | Suitable for favourites, local reports, repairs, permits and sample rent history. |
| Directions | Google Maps intent | Gives practical directions without building a navigation engine. |
| Camera | Android photo picker/camera flow | Supports photo evidence while allowing a clear no-photo path where appropriate. |
| Location | Android location services | Supports distance ordering and editable report pins; manual fallback is required. |
| Notifications | WorkManager/local notifications | Used only after core flows work, for permit/rent reminders. |
| Authentication | Local demonstration profile first | Firebase Auth is not a dependency for the MVP and is only a stretch integration. |

## Proposed package layout

```text
app/
  data/
    model/
    repository/
  ui/
    shared/
    parking/        # Owner A
    streetissues/   # Owner A
    housing/        # Owner B
    cleanairzone/   # Owner B
  navigation/
```

## Guardrails

- No live payments or real Council-account integration.
- No claim that a sample car-park result, report status, rent balance or CAZ answer is official/live data.
- Do not add Firebase, maps, camera, location or notifications until the shared navigation shell and the basic local feature flow work.

