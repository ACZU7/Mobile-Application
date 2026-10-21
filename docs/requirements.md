# Requirements and acceptance criteria

## Product goal

Design and evaluate a usable Android application that helps Bristol residents complete selected parking, street-reporting, travel and housing tasks through clear, accessible, mobile-first flows.

## MVP functional requirements

| ID | Requirement | Owner | Acceptance criteria |
|---|---|---|---|
| FR01 | Browse car parks and Park & Ride locations. | A | A user can open Parking and see sample locations in a list. |
| FR02 | Filter and sort parking results. | A | A user can filter by parking type/area and sort by distance when location is available. A helpful fallback is shown when it is unavailable. |
| FR03 | View parking details and directions. | A | Selecting a location opens a details screen; the Directions action opens the destination in Google Maps or shows a clear fallback. |
| FR04 | Report a street issue. | A | A user can select a category, enter a description, attach an optional photo, review/edit a location pin and submit a local report. |
| FR05 | Track a street issue locally. | A | A submitted report appears in My Reports with a reference and a visible demonstration status. |
| FR06 | Report a housing repair. | B | A user can choose a room and issue type, add a description/photo, select an appointment preference and submit a local repair request. |
| FR07 | View repair progress locally. | B | A submitted repair appears with a reference, selected preference and visible demonstration progress. |
| FR08 | Check a vehicle against prototype CAZ rules. | B | A user can enter a valid-format registration and see a plainly labelled demonstration result plus an Official Checker action. |
| FR09 | Provide shared navigation. | A + B | A user can reach Home, Parking, Streets and Housing from consistent navigation, and Android back behaviour is predictable. |

## Quality-core requirements

| ID | Requirement | Owner | Acceptance criteria |
|---|---|---|---|
| QR01 | Save a parking permit and show an expiry reminder. | A | A user can save a sample permit expiry date and receive a local reminder after notification permission is granted. |
| QR02 | View sample rent balance/history and set a reminder. | B | A user can view local sample data, set a reminder and see saved state after reopening the app. |
| QR03 | Persist saved local data. | A + B | Selected reports, repairs, favourites and settings are retained using local storage. |

## Stretch requirements

- A: parking timer; simulated parking fine lookup/appeal evidence.
- B: Clean Air Zone geofence alert; housing-help decision tree.
- Shared: Firebase Authentication and a richer profile.

## Non-functional requirements

| ID | Requirement | Acceptance criteria |
|---|---|---|
| NFR01 | Accessibility | Text is readable at larger system font sizes; controls have labels; status/errors do not rely on colour alone. |
| NFR02 | Usability | Forms show required fields, validation messages and a clear success/confirmation state. |
| NFR03 | Responsive design | Core screens remain usable on the selected emulator and a second Android screen size. |
| NFR04 | Permission safety | Camera, location and notifications are requested only when needed, with a usable denial fallback. |
| NFR05 | Reliability | Empty, error and permission-denied states are handled without crashes. |
| NFR06 | Privacy | The pilot uses demonstration data and does not collect real payment, tenancy, rent or official permit data. |
| NFR07 | Maintainability | The app follows feature-based MVVM packages, clear names and small reusable composables. |

