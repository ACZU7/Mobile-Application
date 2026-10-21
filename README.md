# Bristol City Companion

Native Android pilot application for Bristol City Council, built for UFCF7H-15-3 Mobile Applications.

The app brings together selected resident services in three areas:

- Parking
- Streets and travel
- Housing

## MVP scope

1. Parking finder: browse nearby car parks and Park & Ride locations, filter results, view details and open directions in Google Maps.
2. Street issue reporting: create a local report with a category, description, optional photo, location pin and status view.
3. Housing repair reporting: create a local repair request with room, problem, optional photo, appointment preference and progress view.
4. Clean Air Zone checker: enter a vehicle registration and receive a clearly labelled demonstration result, with a hand-off to the official checker.

The pilot uses sample/local data until an appropriate public integration is verified. It does not take real payments, submit live Council reports, access real tenancy/rent data, or provide an official CAZ charge decision.

## Technology

- Kotlin
- Android Studio
- Jetpack Compose
- Navigation Compose
- MVVM architecture
- Room for saved local data in the quality-core phase

See `docs/` for requirements, technical decisions and the test plan. See `contributions.md` for individual ownership and contribution evidence.

