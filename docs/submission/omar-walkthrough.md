# Omar assessment walkthrough

Target group runtime: 7 minutes 30 seconds, leaving 30 seconds below the brief's eight-minute limit. Both members must speak and explain their own code. Record from the final reviewed build; the timings below are a rehearsal guide.

| Time | Speaker | Demonstration and explanation |
|---|---|---|
| 0:00–0:20 | Omar | Introduce Bristol Connect as a native Android pilot with local/sample data. Show the Home service grid and navigation. |
| 0:20–1:15 | Omar | Parking: filter EV charging, open matching details, confirm external directions, show location on request and explain the denial fallback. Explain `ParkingViewModel`, immutable UI state, repository filtering and haversine distance sorting. |
| 1:15–1:50 | Omar | Permit: show fictional Zone CM, permit expiry and reminder. Explain why one permit uses DataStore, why WorkManager survives process restarts, and why notifications require consent. |
| 1:50–2:45 | Omar | Streets: show invalid submission, choose a category, enter a fictional description, attach/remove an optional photo and edit the location label. Submit, show the reference and My Reports. Explain validation, private photo copies, Room and save-before-confirmation. The map pin is illustrative, not GPS. |
| 2:45–3:35 | Omar | Profile → Open staff demonstration. Filter reports, update a report status and show the matching resident status. Show report map/analytics, then change sample parking spaces and show the finder update. Explain shared repository flows and saving before publishing an update. |
| 3:35–4:00 | Omar | Show `StreetStorageTest`, `ParkingAdminTest`, migration/device results and one responsive screenshot. Explain one defect found and fixed: confirmation on the main thread or Home Back navigation. State emulator/physical-device limits honestly. |
| 4:00–6:50 | Mohammad | Demonstrate and explain Housing repair/progress, saved Room data, rent/history/reminders, and CAZ validation/prototype outcomes/official checker. Mohammad chooses his code examples and confirms his contribution. |
| 6:50–7:30 | Both | Explain prototype limitations, testing evidence and next steps. State that no live Council reports, payments, official permit boundaries or authoritative CAZ decisions are provided. |

## Code questions to rehearse

- How do composables render state while ViewModels own user actions?
- Why does a failed database write leave a draft intact instead of showing confirmation?
- How does the version 1-to-2 migration preserve Mohammad's Housing table?
- Why use Room for report lists and DataStore for a single permit/reminder preference?
- Why copy a picker URI into private storage, and how is photo removal constrained to that folder?
- How do location denial, no Maps handler and no camera permission leave a usable journey?
- Why is the admin screen a local demonstration rather than a secured production staff system?
- Which work was AI-assisted, what did you check, and can you explain each accepted change?

## Recording and submission

Use screen recording and your own spoken explanation; include code as well as UI. Keep the combined video below eight minutes. If hosting the video on OneDrive, add the accessible link to the mini report and verify the marker can open it. Each member submits their own ZIP. Do not present this script as evidence that either member has recorded or reviewed the work.
