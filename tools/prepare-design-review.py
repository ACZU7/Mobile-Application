"""Make a review copy of the supplied design; leave the source document untouched."""
import sys
from pathlib import Path
sys.path.insert(0, str(Path(__file__).resolve().parents[1] / '.tooling/python'))
from docx import Document
from docx.shared import Inches

root = Path(__file__).resolve().parents[1]
source = Path(sys.argv[1])
document = Document(source)
replacements = {
    'Owner A and Owner B': 'Omar Abdelatty and Mohammad AlAbwah',
    'Stage 2 design draft - research, scope and requirements': 'Design review edition — 25 November 2026',
    '5.1 Low-fidelity wireframes to create': '5.1 Low fidelity wireframe coverage',
    'Fused Location Provider and Google Maps/Maps Intent, with graceful fallback when location is unavailable.':
        'Android LocationManager for on-request location; OpenStreetMap via osmdroid for embedded maps; Google Maps, geo or browser intent for directions, with a no-handler fallback.',
    'Room for saved local demonstration data such as favourites, submitted reports, repairs, permit and rent history.':
        'One shared Room database for Street Reports and Housing repairs. Preferences DataStore stores the single sample permit and reminder choices. Existing parking samples use locally saved admin overrides. Favourites are outside the implemented scope.',
    'Deep blue for primary actions; teal/green only for positive statuses; amber/red with text labels for warnings/errors.':
        'Approved composite palette: teal #2A565F primary, coral #D86347 action accent, green #357544 success, white surfaces and #F5FBF8 tinted cards. Status chips include text and dedicated colour tokens.',
    'The composites apply the visual direction from Section 7: a civic blue primary colour, high-contrast text, Material-style cards, large touch targets and simple task-oriented navigation. They are proposed visual designs rather than screenshots of the final application.':
        'These initial composites record the original visual exploration. Appendix C contains the later approved teal/coral composite set used for implementation. Appendix D distinguishes current implementation choices and shows the running app.',
    'Purpose. This document defines the initial product scope, evidence base and design direction for Bristol City Companion. It is written as a client-facing pilot proposal. It will be updated with final wireframes, composites and test evidence as the project progresses.':
        'This document presents the scope, research, users, requirements and visual design of Bristol Connect, the Bristol City Companion pilot. The original proposal and wireframes are retained; the approved composites and implementation review explain how the design developed. This review edition needs both group members’ agreement before submission.',
}
def update(paragraph):
    if paragraph.text in replacements:
        paragraph.text = replacements[paragraph.text]
    elif paragraph.text == 'A':
        paragraph.text = 'Omar'
    elif paragraph.text == 'B':
        paragraph.text = 'Mohammad'

for paragraph in document.paragraphs:
    update(paragraph)
for section in document.sections:
    for part in (section.header, section.footer):
        for paragraph in part.paragraphs:
            for run in paragraph.runs:
                run.text = run.text.replace('Design Document Draft', 'Design review edition').replace(
                    'Draft prepared 21 October 2026', 'Review edition 25 November 2026')
for table in document.tables:
    for row in table.rows:
        for cell in row.cells:
            for paragraph in cell.paragraphs:
                update(paragraph)

document.add_page_break()
document.add_heading('Appendix C Approved visual design', 1)
document.add_paragraph('The approved twelve-screen composite defines the teal/coral palette, service grid, maps, reporting flows, Staff dashboard and shared navigation. The repository specification is docs/design/ui-spec.md. The initial composites in Appendix B are earlier exploration rather than the final design authority.')
document.add_picture(str(root / 'docs/design/ui-mockups.png'), width=Inches(6.0))
document.add_paragraph('Approved composites: Home 2; Parking 3–4; Streets 5–7; Housing 9–10; Staff 11. Login and translated settings remain design concepts rather than completed authentication/localisation features.')

document.add_page_break()
document.add_heading('Appendix D Implementation review', 1)
document.add_paragraph('The completed Parking and Streets journeys use local demonstration data. Maps show real OpenStreetMap tiles but sample car parks and a fictional permit zone. A Street Report’s location label can be edited; its map pin remains a fixed illustrative location. This distinction avoids presenting sample coordinates as GPS reports.')
document.add_paragraph('Home uses the existing bridge illustration, a guest welcome, service search and the six approved service tiles. It links to saved Street Reports, with a status preview, and provides five-item navigation including a local demonstration Profile. The illustration and guest wording replace the composite’s photographic named-user header because the MVP has no authenticated personal profile.')
document.add_paragraph('The Staff demonstration follows mockup 11: report filters, cards, status changes, map and summary counts. Status updates are saved before being published to the resident views. At Omar’s request a Parking tab also edits spaces, hourly price, EV charging and category for the existing fictional car parks. It has validation and local persistence. It is not a secured production staff portal.')
document.add_paragraph('Housing and CAZ remain Mohammad’s responsibilities. Housing photos remain unavailable in the current build; the implemented flow must not be described as complete photo support. Saved favourites, Firebase authentication, Arabic translation and stretch services are not part of the implemented MVP. RTL layout tests are not translation tests.')
document.add_paragraph('The original research references and access dates are retained from the source design. Final implementation evidence, device configurations and limits are in docs/evidence/finalization and docs/evidence/phase4. Both members should review this edition against their agreed scope and the supplied Blackboard template before submission.')

document.add_page_break()
document.add_heading('Appendix E Application in context', 1)
document.add_paragraph('Screenshots of the running Android pilot show the implemented Home and Staff report views. They are captured emulator outputs; all report and parking information is illustrative.')
table = document.add_table(rows=1, cols=2)
for cell, name in zip(table.rows[0].cells, ['01-home.png', '02-staff-reports.png']):
    cell.paragraphs[0].add_run().add_picture(str(root / 'docs/evidence/finalization' / name), width=Inches(2.5))
document.add_paragraph('GenAI support assisted implementation, debugging, testing and documentation preparation. Students remain responsible for reviewing the work and explaining the accepted code and design decisions. This review copy does not record a completed peer approval or assessment recording.')
output = root / 'docs/submission/Design_Document_Review.docx'
document.save(output)
print(output)
