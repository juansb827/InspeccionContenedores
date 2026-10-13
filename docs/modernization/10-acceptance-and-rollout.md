# 10 — End-to-end acceptance and rollout

Status: proposed. Depends on steps 01–09, including documented optional deferrals. Outcome: a verified in-place upgrade and maintainable project handoff.

## Work sequence

1. Assemble an acceptance matrix using the feature IDs from step 01 and evidence accumulated in every phase. Separate automated results, emulator checks, real-device checks and operator acceptance. Historical camera/gallery reports do not prove authenticated business workflows.
2. Exercise complete workflows against an isolated representative SQL Server database and photo-upload destination. Use a physical inspection device and printer for behavior emulators cannot establish.
3. Install an update over a representative supported earlier build, with the same identity and retained data. Also test an update from the immediately preceding rollout version. Verify drafts, pending operations, media access and settings before and after the first successful save.
4. Run the documented rollback/recovery exercise, including edits created after the update. Reverting code cannot undo remote commits, restore explicitly deleted media, or automatically downgrade a changed local schema.
5. Pilot with a small agreed operator/device group. Define observation length and success thresholds using the baseline rather than guessing a calendar date. Expand only after failures are understood and recovery is demonstrated.
6. Update feature behavior docs, architecture maps, contributor instructions and test/runbooks to match shipped reality. Remove transitional adapters only once there are no callers or supported compatibility paths depending on them. Mark each plan phase completed or explicitly deferred, with evidence links.

## Required acceptance coverage

| Workflow | Evidence required |
| --- | --- |
| Access/settings/reference data | Valid/disabled/missing inspector, unavailable service, valid settings recovery and consistent error display |
| Entry/exit inspection | Supported document types; with/without turno; create/read/modify; required fields; document IDs and confirmed server result |
| Container/booking | Existing/new/changed code; booking membership/balance; linked updates and invalid operations |
| Damage recording | Add/edit/remove/clear; costs; photo association; read-only restrictions; compact/large-text layouts |
| Drafts/recovery | Save/reopen, rotation/resize, process restart, corrupt storage, interrupted submission and no data loss |
| Camera/gallery/video | Permission denial, interrupted capture, unavailable/deleted media, album identity, mixed ordering, local-only video preservation |
| Uploads | No unintended automatic start, partial failure/acknowledgement, retained public photos, no duplicate business submission |
| Printing/export | Real printer connection/denial/failure, receipt content, explicit retry, readable export and viewer permissions |
| Upgrade | Released fixtures and representative installed data preserved without uninstall or data clear |

## Release evidence and stop conditions

Record the commit, build toolchain, APK checksum, version/signing identity verification, test reports, device/window/text matrix, screenshots, sanitized service fixture version, known limitations and recovery procedure. Release artifacts must exclude debug-only fixture hosts and test configuration. Keep credentials and production records out of retained evidence.

Stop expansion on lost/unreadable drafts, unexpected media deletion, duplicate inspection commits, partial atomic writes, incorrect costs, inaccessible critical fields, or broken printing required by operations. Retain affected state for recovery and reconcile remote outcomes before retry. Record crashes, save/lookup latency, restore failures, resource growth, and operator-reported regressions; collect only the operational data actually needed.

## Exit criteria

- [ ] Every critical behavior requirement has passing evidence or an explicitly accepted release limitation.
- [ ] Remote query migration covers the selected scope and no unknown legacy writer remains.
- [ ] Screenshot defects are resolved across the agreed matrix and operator workflows remain recognizable.
- [ ] Upgrade and post-upgrade rollback/recovery exercises preserve user work.
- [ ] Operators complete the pilot's inspection/printing/media scenarios successfully.
- [ ] Documentation and `AGENTS.md` describe the shipped project rather than the intended destination.
- [ ] Optional Room/platform deferrals have a recorded rationale and owner; backend API implementation remains explicitly outside this plan and is not a release dependency.

Completion of this plan does not itself authorize a production deployment. Deployment is a later execution task using the reviewed release artifact and the project's actual distribution process.
