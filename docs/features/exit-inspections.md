# Exit inspections

Status: S/T/E for tested local boundaries; no authenticated execution or operator walkthrough. Reviewed 2026-10-13, app 1.10 at `0a365c1`. Reviewer needed: patio/transport operator.

## Purpose and prerequisites

Choose “Salida” under “Linea Naviera” or “Empresa de Transporte”. Patio has “INFORMACION” and “DAÑOS”; transport has “INFORMACION” only. Load container/business context and inspection date before “Guardar”; use of turnos follows company configuration. General media remains available for an editable new exit; exit damage media is browse-only.

## Behavior and validation

| ID | Current visible behavior | Evidence |
| --- | --- | --- |
| EXIT-001 | New exit refuses a container outside the yard: “El contenedor NO se encuentra en el patio”. Required fields and container/line matching also apply. | S |
| EXIT-002 | For a patio exit with nonempty booking information, a membership failure reports “El contenedor no pertenece al Booking”; exhausted balance reports “El booking ya fue atendido totalmente”. Empty booking information skips these checks. Transport and modification modes skip this booking validation branch. | S; intent unresolved |
| EXIT-003 | Loading an in-yard container retrieves its existing damages. Exit cannot add/remove damages or capture/delete damage media, but can browse available albums. | S/T for media admission |
| EXIT-004 | Exit preserves the container's originating document identity. Save writes exit information without inserting a new damage list. Without a turno, save obtains a document number. | S; turno availability unresolved |
| EXIT-005 | Save, post-save printing, draft cleanup, partial local failure and cancellation follow ENTRY-005 and the entry recovery description. Submission does not upload media. | S |
| EXIT-006 | “Modificar Documento” opens today's exit; another day reports “Esta guia no es de hoy”. Modification replaces exit information without replacing entry damages; capture/deletion and draft-save controls are restricted. | S |

## Cancellation, failures and recovery

[Entry recovery](entry-inspections.md) describes the shared save/back paths. [Pending inspections](pending-inspections.md) describes explicit local drafts. A failed lookup uses “Sin Conexion” and asks to retry. Reopening a server document does not prove that local media associations are recoverable. No operator acceptance establishes timeout/double-save behavior.

## Persistence and side effects

Exit updates business document information; its damage view refers to existing entry damage data. Public photos/videos survive form clearing and resumed-draft removal after submission. Printing is separately chosen. [Storage inventory](../architecture/storage-and-side-effects.md) records retention boundaries.

## Acceptance examples

| ID | Given | When | Then | Verification |
| --- | --- | --- | --- | --- |
| EXIT-001 | Container outside yard | New exit is saved | Yard rejection | S; ACCEPT-02 |
| EXIT-002 | Loaded exhausted patio booking | Save is attempted | Booking balance rejection | S; ACCEPT-03 |
| EXIT-003 | Exit damage album | Operator browses | Media visible; capture/deletion unavailable | T: GalleryAlbumsTest; device gap ACCEPT-04 |
| EXIT-004 | Valid exit with existing damages | Save completes | Exit saved; no new entry damage insertion | S; ACCEPT-02/03 |

## Evidence and questions

Source: [validation/loading](../../app/src/main/java/com/juans/inspeccion/ui/inspection/InspectionInformationFragment.java), [damage controls](../../app/src/main/java/com/juans/inspeccion/ui/inspection/InspectionDamageFragment.java), [save/load boundary](../../app/src/main/java/com/juans/inspeccion/data/remote/sqlserver/InspeccionDataSource.java), [album admission tests](../../app/src/test/java/com/juans/inspeccion/ui/media/gallery/GalleryAlbumsTest.java). Confirm whether absent booking information should allow save, server-loaded damage costs, available turno modes and media availability for reopened documents (ACCEPT-02/03).

## Step 02 regression evidence

InspectionRulesTest, FormBindingTest, InspeccionDataSourceTest and InspectionSubmissionTest execute yard/booking bypasses, request composition/day checks and shared failure/cleanup. Actual lookup/lifecycle and SQL effects remain GATE-04-01/05-01 / ACCEPT-02/03. See [executed tests and precise gates](../testing/step-02-regression.md). Helper/request tests do not establish operator, full-screen, device or service acceptance.
