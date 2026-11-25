// Run after npm install --prefix .tooling docx. This generates an unsigned review document.
const fs = require('fs');
const path = require('path');
const { Document, Packer, Paragraph, TextRun, HeadingLevel, PageBreak, Footer, PageNumber } = require('../.tooling/node_modules/docx');
const out = path.join(__dirname, '../docs/submission/Contribution_Mini_Report.docx');
const { studentNumber } = JSON.parse(fs.readFileSync(path.join(__dirname, '../.tooling/submission-details.json'), 'utf8').replace(/^\uFEFF/, ''));
if (!/^\d+$/.test(studentNumber)) throw new Error('Provide your studentNumber in the local .tooling/submission-details.json file.');
const heading = text => new Paragraph({ text, heading: HeadingLevel.HEADING_1, spacing: { before: 180, after: 100 } });
const para = text => new Paragraph({ children: [new TextRun(text)], spacing: { after: 120 } });
const page = () => new Paragraph({ children: [new PageBreak()] });
const children = [
  new Paragraph({ text: 'Bristol Connect contribution mini report', heading: HeadingLevel.TITLE }),
  para('UFCF7H-15-3 Mobile Applications | 2026/27 | Prepared 25 November 2026'),
  para('Review copy — requires both members’ agreement and signatures. This record identifies the implemented work and development environment for individual assessment.'),
  para(`Omar Abdelatty: ${studentNumber}. Mohammad AlAbwah: student number to confirm. Group ID: to confirm.`),
  heading('Individual contributions'),
  para('Omar Abdelatty — Parking finder, sample-data model and repository, type/area/EV filters, details, confirm-before-opening directions and no-handler fallback; on-request location permission and distance ordering; sample permit/zone screen, DataStore and WorkManager expiry reminder; Street Issue validation, optional CameraX/gallery photos, local confirmation/reference/status and Room persistence; real OpenStreetMap maps; local Admin/Staff dashboard with report status/category filters, map, counts, persisted status changes and editable parking samples. Added tests, device evidence and integration fixes.'),
  para('Omar evidence: ui/parking, ui/streetissues, ui/maps, ui/dashboard/StaffScreen.kt and its supporting DashboardViewModel; data/model/CarPark.kt and StreetReport.kt; ParkingRepository, StreetReportRepository, StreetReportEntity/Dao and AppDatabase migration; ParkingAdminTest, StreetStorageTest, DashboardUiTest, ExternalFlowUiTest and StreetPersistenceTest. Detailed commits and screenshots are indexed in contributions.md and docs/evidence/.'),
  para('Mohammad AlAbwah — owns the Home dashboard and Profile, alongside the Housing repair wizard, validation, confirmation and progress; Housing Room persistence; sample rent account, history and notification reminder; Clean Air Zone validator, demonstration rules/results and official checker hand-off; original shared theme and components. His authored branch history is retained in the combined integration branch. The Home/Profile code prepared by the coding agent in the final handover is support pending Mohammad\'s review, not a claim that he authored that code. Home/Profile is excluded from Omar\'s individual contribution. Mohammad must review and confirm this description.'),
  para('Mohammad evidence: ui/housing, ui/cleanairzone, domain/cleanairzone, RepairRepository, RentRepository, RepairRequestEntity/Dao, Housing/rent/CAZ tests and their evidence folders. See contributions.md for original commits and dates.'),
  para('Shared work — requirements, design, navigation, integration, testing and demonstration. The current handover was prepared with coding-agent assistance; agent-operated emulator tests are not human peer review. No claim of equal line counts or unrecorded meetings is made.'),
  page(),
  heading('Development and execution environment'),
  para('Native Android project; Kotlin language; Jetpack Compose and Material 3; feature-based MVVM; Navigation Compose. Minimum SDK 26, compile SDK 36 and target SDK 36. Android Gradle Plugin 9.0.1; Gradle wrapper 9.1.0; Compose compiler plugin 2.0.21; Compose BOM 2024.09.00; Navigation Compose 2.9.5.'),
  para('Room 2.8.4, DataStore 1.1.7, WorkManager 2.10.3, CameraX 1.4.2, Coil 2.7.0 and osmdroid 6.1.20. The current AGP version provides built-in Kotlin support; the version catalogue’s Kotlin entry identifies the Compose compiler plugin version. Exact resolved dependencies are reproducible through the checked-in Gradle files.'),
  para('Primary final emulator: Medium_Phone_API_37.0, Android API 37, x86_64. Normal display 1080 × 2400 at 420 dpi; simulated tablet and landscape/RTL configurations are documented with the evidence. This does not establish testing on an independent physical device. The local JDK comes from Android Studio’s bundled jbr; exact IDE/JDK build is recorded in the final verification log.'),
  para('Open the project root in Android Studio, install SDK 36, select a JDK compatible with the checked-in Gradle/AGP versions, synchronise Gradle and run the app configuration. The application ID is com.example.mobileapplication. Internet is needed for fresh map tiles; local reporting and sample lists work without live Council services.'),
  heading('Verification and limitations'),
  para('Build/unit/lint command: gradlew.bat test assembleDebug assembleRelease lintDebug. Device command: gradlew.bat connectedDebugAndroidTest. The final evidence index records the exact successful runs, totals, configuration and screenshots. Permission/reboot cases require their documented setup and are skipped by the default suite; skipped checks are not reported as passes.'),
  para('All Council-related records are local/sample data. The app does not submit real reports, accept payment, access a Council account or make an official CAZ decision. Staff access is an openly labelled local demonstration, not production authentication. Housing photo support remains an existing Mohammad-owned gap. Natural notification delivery at a future expiry, TalkBack and physical camera flash must not be claimed without retained evidence.'),
  para('Design changes: real OpenStreetMap tiles replace illustrated maps; a fixed sample Street pin has an editable text label; a single permit/reminder uses DataStore while report lists share Room; Home uses the existing bridge illustration and guest wording. Staff adds parking sample management at Omar’s request. These changes preserve the pilot concept and are documented for group review.'),
  page(),
  heading('Sources and assistance'),
  para('Council service research and comparable apps are referenced in the design document. Third-party libraries are declared in gradle/libs.versions.toml and app/build.gradle.kts. Map attribution remains visible. Material icons and the project’s bridge vector are reused; no live Council data or Council account integration is asserted.'),
  para('GenAI acknowledgement for review: OpenAI Codex assisted Omar with implementation, debugging, automated tests, emulator verification and preparation of documentation. Generated changes require student review, adaptation and understanding. The contribution history and evidence distinguish AI-operated checks from human approval. Each member should amend this acknowledgement to reflect their own actual use before signing.'),
  heading('Video and sign off'),
  para('Group demonstration video: to be recorded by both members; total runtime must be less than eight minutes. Video filename or accessible OneDrive link: __________________________________________.'),
  para('We agree that the reviewed contribution descriptions fairly represent our respective work and that we understand the code and results submitted. Signing this record does not imply that the unsigned review copy has already been approved.'),
  para(`Omar Abdelatty | Student number ${studentNumber}`),
  para('Signature: __________________________________  Date: __________________'),
  para('Mohammad AlAbwah | Student number: __________________'),
  para('Signature: __________________________________  Date: __________________'),
  para('Group ID: __________________  Final reviewed commit: __________________'),
  para(`Submission filename: MACoursework_GroupID_${studentNumber}.zip. Replace GroupID with the confirmed group identifier; each member uploads individually.`)
];
const doc = new Document({
  creator: 'Bristol Connect project', title: 'Bristol Connect contribution mini report',
  styles: { default: { document: { run: { font: 'Calibri', size: 21 }, paragraph: { spacing: { line: 260 } } } } },
  sections: [{ properties: { page: { size: { width: 11906, height: 16838 }, margin: { top: 1000, bottom: 1000, left: 1100, right: 1100 } } },
    footers: { default: new Footer({ children: [new Paragraph({ children: [new TextRun('Bristol Connect | Review copy | '), new TextRun({ children: [PageNumber.CURRENT] })] })] }) }, children }]
});
Packer.toBuffer(doc).then(buffer => { fs.mkdirSync(path.dirname(out), { recursive: true }); fs.writeFileSync(out, buffer); console.log(out); });
