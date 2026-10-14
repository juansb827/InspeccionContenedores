# Pending inspections / drafts

Status: current S/T, with step-03 automated execution evidence. Reviewed 2026-10-13, app 1.10 at `0a365c1`. Reviewer needed: operator/storage owner.

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

## Storage retirement (intentional step-03 behavior change)

Earlier steps characterized opening serialized drafts. Those expectations are superseded; fixture bytes remain as historical evidence.

**DRAFT-006 (S/T/E):** recognized unsupported older drafts are excluded with a notice allowing new drafts in a separate current store. Supported formats remain readable regardless of writer app version. Newer schemas, corrupt/unknown data and I/O errors have distinct outcomes and must be preserved. Do not guess originating app versions or draft counts. [ADR 002](../decisions/002-storage-retirement.md) and the [storage policy](../modernization/storage-version-policy.md) govern this change; [Current storage/recovery](../architecture/draft-schema-v1.md) and [step-03 results](../testing/step-03-cutover-and-moves.md) describe the implementation.

## Acceptance examples

| ID | Given | When | Then | Verification |
| --- | --- | --- | --- | --- |
| DRAFT-001 | Existing draft with same identity | Save again | Existing draft replaced | T: StorageCompatibilityTest identity case |
| DRAFT-004 | Damaged existing store | Save/clear is attempted | Error; previous file preserved | T: StorageCompatibilityTest; UI manual gap |
| DRAFT-006 | Recognized retired collection | The app opens storage | Unsupported notice; isolated source; new drafts possible | T/E: DraftCutoverTest; dedicated local storage runner |

## Evidence and questions

Source: [draft list](../../app/src/main/java/com/juans/inspeccion/ui/pending/PendientesActivity.java), [inspection menus/save](../../app/src/main/java/com/juans/inspeccion/ui/inspection/InspeccionActivity.java), [draft operations](../../app/src/main/java/com/juans/inspeccion/data/local/Pendientes.java), [compatibility tests](../../app/src/test/java/com/juans/inspeccion/Mundo/Storage/StorageCompatibilityTest.java). Confirm “Borrar todo” intent and restored form completeness; the current notice is reviewed through automated local paths, with operator wording/cleanup acceptance still open.

## Historical step 02 regression evidence

At step 02, DraftRecoveryTest covered serialized reopen/backup/error preservation; InspectionSubmissionTest covered real local removal after fake remote outcomes. StorageFormatProbeTest then covered an unwired encoding/envelope preflight only. DRAFT-006 application behavior was planned; real record validation/new-format recovery/notices/no replay required GATE-03-01/02. See [executed tests and precise gates](../testing/step-02-regression.md). Helper/request tests do not establish operator, full-screen, device or service acceptance.

## Step 03 evidence

DRAFT-001–006 use the real current-format reader/writer, with complete field/album/ordering recovery and distinct error outcomes. A notice titled “Estado de almacenamiento” explains retirement and public photo/video preservation. “Entendido” acknowledges it durably; “Cerrar” leaves it pending. The same status remains available in Configuración. New drafts work after recognized old data is isolated; newer, corrupt or unknown current data is preserved and blocks replacement. No public media is inferred into a new draft. See [storage tests and limitations](../testing/step-03-cutover-and-moves.md). The preceding step-02 paragraph is historical evidence of its unwired preflight, superseded by this implementation.
