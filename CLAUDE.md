# Project notes for Claude Code

Bristol City Council pilot Android app (UWE Mobile Applications coursework). Kotlin, Jetpack Compose, Material 3, MVVM. Read `docs/implementation-plan.md` for scope and ownership: Mohammad owns Housing and Clean Air Zone; Omar owns Parking and Streets – don't change the other person's feature without being asked.

## UI design – always follow

- The approved design is `docs/design/ui-mockups.png` and `docs/design/ui-spec.md`.
- Before building or changing any screen, look at the matching screen in the image and read the spec. The finished screen must match the mockup's layout, colours, spacing, icons and wording.
- Use the colour tokens and shared components defined in the spec; never hard-code colours or text.
