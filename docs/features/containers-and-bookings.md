# Containers, turnos and bookings

Status: S/T/E for tested local boundaries; no operator/service acceptance. Reviewed 2026-10-13, app 1.10 at `0a365c1`. Reviewer needed: yard/booking owner.

## Purpose and prerequisites

From inspection information, look up a container or turno, create/modify container details, and inspect booking context. Live lookup requires configured business services and a selected line where requested. Container creation/editing opens a separate form; it is a business-record write independent of saving the inspection.

## Behavior and validation

| ID | Current visible behavior | Evidence |
| --- | --- | --- |
| CONTAINER-001 | Turno lookup reports “No se ha encontrado el turno”, “Este turno ya fue atendido”, or “El tipo de turno no es de :” followed by the expected direction. Accepted turno information populates the inspection and starts date loading. | S |
| CONTAINER-002 | A missing container in new entry without a turno offers “CREAR” / “CANCELAR”. Existing container editing asks “Modificar Contenedor” / “¿Desea modificar el contenedor?” with “MODIFICAR” / “CANCELAR”. | S |
| CONTAINER-003 | Container code editing expects a 4-character sigla, 6-character serie and 1-character verification digit; dates expect AAAAMM. These handlers check lengths, not a documented check-digit algorithm. | S |
| CONTAINER-004 | Type/size selection looks up the ISO code. “Guardar” rejects required-field gaps with “FALTAN CAMPOS OBLIGATORIOS”. Successful creation/modification shows “SE GUARDO EL CONTENEDOR”, returns to the inspection and reloads the resulting code. | S |
| CONTAINER-005 | Editing booking requires line selection; otherwise “DEBE SELECCIONAR UNA LINEA PRIMERO”. A found booking opens “Datos Booking”; no result reports “No se encontro el booking :” followed by its code. Exit membership/balance restrictions are EXIT-002. | S |
| CONTAINER-006 | Pending turno selection uses a “TURNOS” list. Availability differs from saved local drafts shown in “Turnos Sin Guardar”. | S |

## Cancellation, failures and recovery

Back from container editing warns “Atencion:” / “No ha guardado el contenedor, Esta seguro?”. Canceling creation/modification does not save that form. Connection failures request retry and do not establish a successful save. ISO lookup failure uses connection recovery. Reopening after an uncertain save and changed container code require ACCEPT-02/03; there is no verified automatic rollback of earlier container changes when an inspection is abandoned.

## Persistence and side effects

Saving a container can update its associated turno when the code changes. These writes can precede inspection submission. Booking lookup reads business information; its result and yard state influence inspection validation. Reference refresh does not replace these business records.

## Acceptance examples

| ID | Given | When | Then | Verification |
| --- | --- | --- | --- | --- |
| CONTAINER-001 | Already attended turno | Lookup | Attended message; turno not accepted | S; ACCEPT-02 |
| CONTAINER-004 | Required fields missing | Save | Required-field message; stays in form | S; ACCEPT-03 |
| CONTAINER-005 | No selected line | Booking edit | Line-selection message | S; ACCEPT-03 |

## Evidence and questions

Source: [information lookup](../../app/src/main/java/com/juans/inspeccion/Interfaz/Inspeccion_tabs/p1.java), [container form](../../app/src/main/java/com/juans/inspeccion/Interfaz/FichaContenedorActivity.java), [business writes](../../app/src/main/java/com/juans/inspeccion/Mundo/Data/InspeccionDataSource.java). Confirm available turno/no-turno entry paths, allowed code changes, check-digit validation intent, absent booking behavior, and test schema/transaction outcomes. See [acceptance register](../testing/acceptance.md).

## Step 02 regression evidence

InspectionRulesTest covers turno and exit booking branches; FormBindingTest covers date/required metadata; InspeccionDataSourceTest covers real linked-write requests with a fake SQL transport. ISO/code editing/cancel UI and real transaction effects remain GATE-04-01/05-01 / ACCEPT-03. See [executed tests and precise gates](../testing/step-02-regression.md). Helper/request tests do not establish operator, full-screen, device or service acceptance.
