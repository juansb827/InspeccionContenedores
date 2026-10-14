# Entry inspections

Status: S/T/E for tested local boundaries; no authenticated execution or operator walkthrough. Reviewed 2026-10-13, app 1.10 at `0a365c1`. Reviewer needed: patio/transport operator.

## Purpose and prerequisites

Choose “Entrada” under “Linea Naviera” or “Empresa de Transporte” on the main screen. The patio workflow has “INFORMACION” and “DAÑOS”; transport has “INFORMACION” only. Inspection information, container information, a loaded inspection date and reference lists are prerequisites for submission. Use of turnos follows configured company behavior; it is not a separate main-screen choice. Business-service access is required to load live data and submit; saving a loaded draft is a separate local action.

## Behavior and validation

| ID | Current visible behavior | Evidence |
| --- | --- | --- |
| ENTRY-001 | Patio entry provides general media and damage albums; transport entry provides general media without a damage tab. | S |
| ENTRY-002 | “Guardar” checks required fields and rejects a container belonging to another line with “El contenedor no peternece a la linea”. New entry rejects a container already in the yard with “El contenedor YA se encuentra en el patio”. | S |
| ENTRY-003 | Submission requires a loaded inspection date. Date loading failure shows “SIN_CONEXION” and prevents submission. Missing required values shows “Faltan campos obligatorios”. Validation failures do not proceed to submission. | S |
| ENTRY-004 | New entry saves its inspection information and listed damage records. For a workflow without a turno, the document number is assigned on save; entry retains its own number as its originating document. | S; turno availability unresolved |
| ENTRY-005 | Save shows “Guardando”; no reported error leads to the success toast and “Guardó correctamente” / “Desea imprimir?” with “Imprimir” / “No, Gracias”. Declining post-save printing clears the form. | S; receipt completion needs acceptance |
| ENTRY-006 | “Modificar Documento” can open today's entry for modification; another day reports “Esta guia no es de hoy”. Save replaces the entry document and its damage list. This mode hides “Guardar Borrador” and normal “Imprimir”, and disallows new capture/media deletion. | S |

## Cancellation, failures and recovery

Back requests “Salir” / “Desea salir esta Inspeccion?” with “Salir” / “No, Gracias”; this is not an automatic draft save. Pending media must be resolved before menu work can continue. [DRAFT-001](pending-inspections.md) describes explicit draft saving. A reported remote error is displayed without the post-save print prompt. Timeout/uncertain commit, repeated taps and what survives process death during save remain ACCEPT-02; do not assume a failed response means no remote write occurred.

If the document save succeeds but photo naming or draft cleanup fails, the operator sees “El documento se guardó; las fotos/borrador requieren recuperación: ” plus the error. This means remote success with unresolved local recovery, not permission to blindly resubmit.

## Persistence and side effects

Submission writes business records, adjusts photo names used for the document, removes a resumed draft only after successful local cleanup, and offers printing. It does not automatically upload photos or videos. Clearing the form retains public media. [Camera/media](camera-and-media.md) and [printing](printing.md) describe their separate effects.

## Acceptance examples

| ID | Given | When | Then | Verification |
| --- | --- | --- | --- | --- |
| ENTRY-002 | A container already in the yard | New entry is saved | Yard message; no submission | S; ACCEPT-02 |
| ENTRY-004 | Valid new patio entry with damages | Save completes | Header and entry damages saved under assigned document | S; ACCEPT-02/03 |
| ENTRY-006 | An entry from yesterday | Modification is requested | “Esta guia no es de hoy” | S; ACCEPT-02 |

## Evidence and questions

Source: [screen](../../app/src/main/java/com/juans/inspeccion/ui/inspection/InspeccionActivity.java), [information/validation](../../app/src/main/java/com/juans/inspeccion/ui/inspection/InspectionInformationFragment.java), [save/load boundary](../../app/src/main/java/com/juans/inspeccion/data/remote/sqlserver/InspeccionDataSource.java), [main labels](../../app/src/main/res/layout/activity_main.xml). Turno/no-turno initialization has contradictory guards; available operator paths must be confirmed (ACCEPT-02). Required-field membership depends on mode and loaded metadata; do not infer a complete accepted field contract from labels alone. See [technical risks](../architecture/current-system.md).

## Step 02 regression evidence

InspectionRulesTest, FormBindingTest, InspeccionDataSourceTest and InspectionSubmissionTest execute rules, real form metadata/menu, request composition/numbering and failure/local cleanup. Actual authenticated lifecycle/async/printing and uncertain server commits remain GATE-04-01/05-01 / ACCEPT-02. See [executed tests and precise gates](../testing/step-02-regression.md). Helper/request tests do not establish operator, full-screen, device or service acceptance.
