# Assessment handover

Source: the supplied `UFCF7H-15-3 Mobile Applications  2026-27 Mainsit COURSEWORK.docx`, sections 1.1, 1.2 and 9. Deadline stated there: **before 14:00 UK time on 7 January 2027**.

## Required individual submission

The brief requires one ZIP named `MACoursework_GroupID_StudentNumber.zip`, containing:

1. Stage 2 design document in Word or PDF; the same agreed design document for both members.
2. Stage 3 contribution mini report in Word or PDF, agreed and signed by both members, including development environment and emulator details.
3. Android Studio project, including code and resources.
4. Group video file(s), totalling less than eight minutes, or a video link in the mini report. Every member demonstrates and explains their own code.

The student number is held only in the local generated report/package. Group ID has not been supplied. The output ZIP is a review package until its filename, signatures and video are completed. Generated Word files and submission ZIPs are excluded from this public repository.

## Ownership

Omar: Admin/Staff dashboard, Parking and Streets. Mohammad: Home dashboard, Profile, Housing and Clean Air Zone. Shared work covers navigation integration, common components, testing and assessment deliverables. The agent-created Home/Profile implementation has been removed; Home is back to its pre-existing placeholder and Profile is not implemented. Admin has its own navigation tab.

## Prepared material

- `Contribution_Mini_Report.docx`: corrected contribution/environment record for both members to review and sign. This supersedes the earlier `Contribution_Mini_Report_Review.docx`. Home/Profile is assigned to Mohammad and Admin to Omar; no signature, authorship or peer approval is fabricated.
- `Design_Document_Review.docx`: review copy of the existing design draft, with named owners, corrected implementation choices, approved mockups and implementation changes distinguished from the original proposal. The original Desktop document is preserved.
- [Omar walkthrough](omar-walkthrough.md): timed code/UI explanation and rehearsal questions.
- [Final verification](../evidence/finalization/README.md): actual test results and screenshot evidence.
- `tools/package-submission.ps1`: creates the review ZIP from tracked project files and the documents, excluding local caches, credentials and build directories.

To regenerate the mini report, install `docx` with `npm install --prefix .tooling docx`, put your numeric `studentNumber` in the ignored `.tooling/submission-details.json`, and run `node tools/build-mini-report.cjs`. Close the generated report in Word before regenerating it. The design-copy helper accepts the source DOCX path and requires `python-docx` in `.tooling/python`.

After reviewing the documents, run `tools/package-submission.ps1 -GroupId YOUR_GROUP_ID -StudentNumber YOUR_STUDENT_NUMBER`. Do not publish generated assessment documents or archives to the public repository.

## Remaining student actions

- Confirm Group ID and Mohammad's student number; review the design and contribution text together.
- Both members sign the mini report and supply the final video or accessible link.
- Mohammad resolves/accepts his existing Housing photo gap; Omar's work does not claim it is implemented.
- Human peer review and merge of the final feature branch, then verification of the merged commit.
- Check Blackboard for any later assessment announcements before uploading individually.

## Use of AI

Section 9 permits support from GenAI and requires understanding, checking and acknowledgement when its contribution is significant. The mini report includes a factual acknowledgement of coding, debugging, testing and documentation assistance. Both students must review it against their own use and explain all submitted code themselves.
