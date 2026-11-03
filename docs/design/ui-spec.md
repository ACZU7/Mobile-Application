# UI spec – Bristol Connect

The approved mockups are in `docs/design/ui-mockups.png` (12 screens). Every screen must match them: layout, colours, spacing, icons and wording. When this spec and the image disagree, the image wins. Colours below were sampled from the image.

## Colour tokens

| Token | Hex | Used for |
| --- | --- | --- |
| `primary` (teal) | `#2A565F` | Sign in button, selected filter chip, selected nav item, toggles, "Update" button, links, icons on tiles |
| `accent` (coral) | `#D86347` | Main call-to-action buttons (Submit report, Next, Pay rent), active stepper step, map pins, "Report an issue" icon |
| `success` (green) | `#357544` | Success tick circles ("No charge", "You're up to date") |
| `surface` | `#FFFFFF` | Screen background and cards |
| `surfaceTint` | `#F5FBF8` | Highlighted cards (rent balance, selected room tile, result cards) |
| `chipNeutral` | `#F2F3F5` | Unselected filter chips |
| `plateYellow` | `#EBC038` | UK number-plate input |
| `plateBlue` | `#233471` | "GB" strip on the left of the number-plate input |
| Status – Submitted | bg `#DAE8FB`, text dark blue | Status chip |
| Status – In progress | bg `#F6E0B6`, text dark amber | Status chip |
| Status – Fixed / Completed | bg `#DAEFE1`, text dark green | Status chip |
| Status – New | bg `#F3D2C6`, text dark coral | Status chip (staff) |
| Text primary | near-black `#1C1C1C` | Titles, body |
| Text secondary | grey `#6B6B6B` | Subtitles, dates, references (e.g. REF-231456) |

Put these in `ui/theme/Color.kt` and map them into the Material 3 colour scheme (`primary`, `secondary` = accent, etc.). Never hard-code colours in screens.

## Typography and shape

- Font: a clean sans-serif (system default / Roboto is fine).
- Screen title in top bar: 18sp, SemiBold. Big hero text (e.g. "£0.00", "Good morning, Sam"): 28–32sp, Bold.
- Section headings ("Nearest car parks", "Which room is the problem in?"): 16–18sp, SemiBold.
- Body 14sp; captions, references and dates 12sp in text secondary.
- Corner radius: cards and tiles 12dp; buttons 12dp; chips fully rounded; text fields 10dp.
- Screen padding 16dp; 12dp between cards; 8dp inside small groups.
- Cards: white with a very light border or 1dp elevation, never heavy shadows.

## Shared components (build once in `ui/shared`, reuse everywhere)

- **TopBar**: white, back arrow on the left, left-aligned title. No colour fill.
- **BottomNavBar**: 5 items – Home, Parking, Streets, Housing, Profile – icon above label. Selected = teal icon and label; others grey.
- **PrimaryButton**: full width, 48dp tall, coral fill, white text.
- **SecondaryButton**: full width, white with teal outline and teal text (e.g. "Continue as guest").
- **StatusChip**: small rounded chip using the status colours above.
- **ServiceTile**: square card with a centred icon and a 2-line label underneath (Home grid).
- **SelectableTile**: icon + label tile; selected = teal 1.5dp border and `surfaceTint` background (room picker).
- **Stepper**: numbered circles joined by a line; active = coral filled circle; upcoming = grey outline; labels under each.
- **ListRowCard**: thumbnail or icon on the left, title + secondary line, chevron on the right.
- **OutlinedField**: outlined text field with a leading icon and placeholder.

## Screens

1. **Login** – Bristol bridge illustration and "Bristol Connect" title in teal, subtitle "Council services for a cleaner, fairer Bristol"; email and password fields; teal "Sign in"; "or"; outlined "Continue as guest"; landscape illustration at the bottom.
2. **Home** – header image of Bristol with "Good morning, {name}" and "How can we help you today?"; search field; 3×2 grid of ServiceTiles (Find parking, Permit zones, Report an issue, Clean Air Zone, Report a repair, Rent account); "Your street report – In progress" card; bottom nav.
3. **Car park finder** – search bar, filter chips (All, Multi-storey, Surface, EV charging), map with pins, bottom sheet "Nearest car parks" with photo, name, distance, spaces, price and chevron.
4. **Permit zone checker** – map with the zone shaded coral and the user's dot; result card "You are inside Zone CM, Permit required 9am–5pm Mon–Fri"; "Your resident's permit" card with expiry; "Remind me before it expires" toggle.
5. **Report a street issue – camera** – full-screen camera, close and flash buttons, category chips (Pothole, Street light, Fly-tipping, Graffiti; selected = coral), shutter, gallery and flip buttons.
6. **Report a street issue – details** – photo thumbnail + "Add another photo" tile, location map with address and edit pencil, Issue type dropdown, Description with character counter (0/500), coral "Submit report".
7. **My reports** – filter chips (All, Submitted, In progress, Fixed); top card with photo, title, reference and a 3-stage progress timeline (Submitted → In progress → Fixed, coral dots); list rows with StatusChip and date.
8. **Clean Air Zone checker** – illustration and "Check if your vehicle can drive in Bristol's Clean Air Zone"; yellow UK number-plate input; result card with green tick ("No charge – your vehicle meets the standard"); "Vehicle details" table (Make, Model, Fuel type, Euro standard).
9. **Report a repair** – Stepper with 4 steps: Room, Problem, Photos, Appointment. Step 1 title "Which room is the problem in?" with subtitle; 2×2 grid of SelectableTiles (Kitchen, Bathroom, Bedroom, Living room); coral "Next" at the bottom. Later steps follow the same layout.
10. **Rent account** – `surfaceTint` card with house icon, "£0.00", "You're up to date" and green tick; "Next payment" row with date and amount; coral "Pay rent"; list rows: Payment history, View your tenancy, Report a housing issue, Update your details.
11. **Staff dashboard** – title "Bristol Connect Staff" with avatar; segmented tabs (Reports, Map, Analytics); two dropdown filters; "12 reports"; report cards with photo, title, reference, date, StatusChip, and on the selected card a "Change status" dropdown with a teal "Update" button.
12. **Settings – Arabic** – full right-to-left layout; profile header; rows with icons (language, notifications, privacy, help, about); language picker showing English, العربية (ticked), Polski; bottom nav mirrored.

## Rules for building screens

- Match the mockup for the screen you are building; open `docs/design/ui-mockups.png` and compare before finishing.
- Use only theme colours, typography and the shared components above.
- All text in `strings.xml`. Icons from Material Symbols, with content descriptions.
- Must also work in dark mode, landscape, on a tablet and in Arabic (RTL) without breaking the layout.
- Sample data shown in the UI must be clearly labelled as demonstration data where the implementation plan requires it.
