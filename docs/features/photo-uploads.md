# Photo uploads

Status: dormant current behavior S/T; retirement P. Reviewed 2026-10-13, app 1.10 at `0a365c1`. Reviewer needed: maintenance owner.

## Purpose and entry points

Historical photo-upload work remains in the repository. There is no active operator upload workflow started by current inspection save, main-screen handlers or network notifications. An inspection success message does not mean files reached a remote photo service.

| ID | Behavior | Evidence |
| --- | --- | --- |
| UPLOAD-001 | Saving inspections/capturing media does not automatically start photo uploads. Videos never enter the photo queue. | S/T video exclusion |
| UPLOAD-002 | Existing helper tests exercise incremental photo-upload acknowledgement, retry of remaining photos and retention of public gallery copies using fakes. These are helper contracts, not acceptance of a reachable upload screen or service. | T |
| UPLOAD-003 | The dormant background service is not a usable configured upload feature. No operator-visible successful upload, cancellation or end-to-end retry is established. | S |
| UPLOAD-004 | **P, accepted, not implemented:** the old unused queue is retired without compatibility/import or replay. Upload unavailability must not block inspection saving. Any remaining upload controls must report unavailability rather than throw or claim success. | P |

## Failures, persistence and side effects

The current queue persists acknowledgement state; helper transfer failures retain retry work and close streams/connections. Do not infer that the app schedules those retries automatically. Enabling the dormant path would introduce remote effects and is outside this baseline. No FTP endpoint was contacted. There is no requirement to restore upload functionality in this modernization. Retirement must preserve public photos/videos and independently audit publication/capture journals.

## Acceptance examples

| ID | Given | When | Then | Verification |
| --- | --- | --- | --- | --- |
| UPLOAD-001 | Valid inspection with media | Save completes | No automatic upload start | S; end-to-end inspection gap ACCEPT-02 |
| UPLOAD-002 | Fake transfer failing on second photo | Helper retries | First acknowledgement persisted; public copy retained | T: UploadQueueTest |
| UPLOAD-004 | Recognized retired queue | Future cutover starts | No replay/new upload work; inspection remains usable | P; step-02/03 gate |

## Evidence and open items

Source: [inspection upload stub](../../app/src/main/java/com/juans/inspeccion/Interfaz/InspeccionActivity.java), [dormant service](../../app/src/main/java/com/juans/inspeccion/Other/SimpleService.java), [network receiver](../../app/src/main/java/com/juans/inspeccion/Other/NetWorkChangeReceiver.java), [queue helper tests](../../app/src/test/java/com/juans/inspeccion/Mundo/Storage/UploadQueueTest.java). [ADR 002](../decisions/002-storage-retirement.md) supersedes historical requirements to import/replay this queue. No active upload acceptance item is required; preventing replay at cutover is ACCEPT-07.
