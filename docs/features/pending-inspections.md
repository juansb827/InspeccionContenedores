# Pending inspections / drafts

Status: current S/T; future cutover P. Reviewed 2026-10-13, app 1.10 at `0a365c1`. Reviewer needed: operator/storage owner.

## Purpose and entry points

“Guardar Borrador” stores unfinished inspection work locally. “Turnos Sin Guardar” opens “Registros Pendientes”. These local drafts are distinct from remotely pending turnos. Draft save is hidden in read-only and modification modes.

## Current behavior

| ID | Behavior | Evidence |
| --- | --- | --- |
| DRAFT-001 | Draft save requires a loaded date and sufficient loaded information; otherwise “No ha cargado ningun turno o contenedor”. Success shows “Se ha agregado el turno lista de pendientes” and closes the inspection. Saving the same inspection updates its draft rather than adding a duplicate. | S/T identity updates |
| DRAFT-002 | The list shows turno, direction and date. Selecting a row reopens its inspection/document type for editing; the list reloads on return. | S |
| DRAFT-003 | “Borrar todo” immediately clears readable local drafts without a confirmation in its handler. Public photos/videos remain. | S |
| DRAFT-004 | An unreadable store displays “Borradores no disponibles” and its recovery message; the visible list is empty but clearing is blocked. Save refuses to overwrite unreadable data. Missing storage normally opens an empty list. | S/T |
| DRAFT-005 | Successful submission of a resumed draft removes that draft after photo naming succeeds. Local cleanup failure reports that the document saved but photos/draft need recovery. A reported remote error does not run this cleanup. | S |

## Cancellation, failures and recovery

Leaving an inspection does not automatically save a draft. Capture recovery can retain inspection snapshots separately, so recovery of a captured inspection is not proof of general form autosave. Storage failures retain the previous durable collection; edits in memory are not guaranteed durable after process death. Reopen, repeated save, deletion and failure examples need ACCEPT-02/07.

## Persistence and planned changes

Current source still opens earlier serialized drafts (T), without a separate storage version envelope. This is current evidence, not the future compatibility requirement.

**DRAFT-006 (P, accepted, not implemented):** recognized unsupported older drafts will be excluded with a notice allowing new drafts in a separate current store. Supported formats remain readable regardless of writer app version. Newer schemas, corrupt/unknown data and I/O errors have distinct outcomes and must be preserved. Do not guess originating app versions or draft counts. [ADR 002](../decisions/002-storage-retirement.md) and the [storage policy](../modernization/storage-version-policy.md) govern this change; step 02 tests recognition, step 03 implements cutover before package moves.

## Acceptance examples

| ID | Given | When | Then | Verification |
| --- | --- | --- | --- | --- |
| DRAFT-001 | Existing draft with same identity | Save again | Existing draft replaced | T: StorageCompatibilityTest identity case |
| DRAFT-004 | Damaged existing store | Save/clear is attempted | Error; previous file preserved | T: StorageCompatibilityTest; UI manual gap |
| DRAFT-006 | Recognized retired collection | Future cutover reads it | Unsupported notice; isolated source; new drafts possible | P; no current implementation/test |

## Evidence and questions

Source: [draft list](../../app/src/main/java/com/juans/inspeccion/Interfaz/PendientesActivity.java), [inspection menus/save](../../app/src/main/java/com/juans/inspeccion/Interfaz/InspeccionActivity.java), [draft operations](../../app/src/main/java/com/juans/inspeccion/Mundo/Pendientes.java), [compatibility tests](../../app/src/test/java/com/juans/inspeccion/Mundo/Storage/StorageCompatibilityTest.java). Confirm “Borrar todo” intent and restored form completeness; future notice wording/cleanup UX remain implementation review items, not current behavior.

## Step 02 regression evidence

DraftRecoveryTest covers current serialized reopen/backup/error preservation; InspectionSubmissionTest covers real local removal after fake remote outcomes. StorageFormatProbeTest covers an unwired encoding/envelope preflight only. DRAFT-006 application behavior remains planned; real record validation/new-format recovery/notices/no replay require GATE-03-01/02. See [executed tests and precise gates](../testing/step-02-regression.md). Helper/request tests do not establish operator, full-screen, device or service acceptance.
