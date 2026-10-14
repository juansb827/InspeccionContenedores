# Damage recording

Status: S/T/E for tested rule boundaries; no operator walkthrough. Reviewed 2026-10-13, app 1.10 at `0a365c1`. Reviewer needed: inspection/tariff owner.

## Purpose and entry points

“DAÑOS” on patio inspections shows damage rows, a damage-entry form and “COSTO TOTAL : $” followed by the total. New editable Entrada permits adding/removing damages. Salida and read-only inspections refuse add/remove actions. Modification has different media restrictions; do not assume the damage form is universally read-only in modification mode.

## Behavior and validation

| ID | Current visible behavior | Evidence |
| --- | --- | --- |
| DAMAGE-001 | Select sequence, “Ubicacion”, “Componente”, “Daño”, “Metodo”, “Largo”, “Ancho”, “Cantidad” and “Con Cargo A”. Location choices depend on sequence. Selecting a repair method initializes length, width and quantity to 1. | S |
| DAMAGE-002 | Charge selection first requires a line and container size; with that context present, blank location, component, damage type, repair method or quantity is rejected in that order. Dimension-based methods require length and width; the alternative size-based branch requires container size. | S |
| DAMAGE-003 | Charge choices are “NOAPLICA”, “CLIENTE”, “LINEA”. Adding requires a nonempty charge selection: “DEBE INDICAR A CARGO DE QUIEN SE HACE LA REPARACION”. Required-field validation occurs on charge selection; the add action does not independently repeat the full validation. | S; suspected gap |
| DAMAGE-004 | Charged damage cost is material rate × quantity plus labor hours × labor rate × quantity, with IVA applied to that sum. “NOAPLICA” yields zero. Missing/failed tariffs yield zero; missing tariffs show “Tarifas no encontradas”. This fallback needs business acceptance. | S |
| DAMAGE-005 | Adding appends and renumbers rows, updates the total and clears the entry form. Removing the selected row subtracts its stored cost and renumbers the remaining rows. Removing a damage unlinks photos without deleting public media. | S |
| DAMAGE-006 | Selecting an existing row fills the form and its album. The update button currently performs no update. Adding from a selected damage creates a new row and does not copy its photos. | S; quirk, not accepted edit semantics |
| DAMAGE-007 | Unsaved editing-album photos can attach when adding a new damage. Switching rows with uncommitted photos asks “Borrar Fotos?” and warns that photos will be lost; public copies are retained. This warning is based on photos, so a video-only editing album is not covered by that prompt. | S; media association gap |

## Cancellation, failures and recovery

Canceling a charge/list dialog does not constitute submission. Tariff lookup temporarily disables form controls; failures must be reviewed with a test tariff service. Reopening, repeated total recalculation, decimal formatting and modifying a selected row need ACCEPT-03. Existing source has a non-resetting total recalculation path; repeated recalculation is a suspected defect, not an accepted sum requirement.

## Persistence and side effects

Damage rows are saved with entry inspections and local drafts. Exit shows existing rows. Media deletion is an explicit [gallery action](camera-and-media.md), separate from clearing/removing a damage. No upload follows addition.

## Acceptance examples

| ID | Given | When | Then | Verification |
| --- | --- | --- | --- | --- |
| DAMAGE-002 | Valid line/size, missing location and quantity | Charge is selected | Location rejection first | T/E: DaniosManagerTest rule boundary; charge UI remains ACCEPT-03 |
| DAMAGE-004 | Material 10, quantity 2, hours 1, labor rate 5, IVA 10% | Cost is found | Damage total 33 | T/E: DamageCostLookupTest worked result; no operator acceptance |
| DAMAGE-006 | Selected existing row | Update button is pressed | No update is currently executed | S; operator decision ACCEPT-03 |
| DAMAGE-005 | Row with public media | Row is removed | Row unlinked, public bytes retained | S; media unit evidence only |

## Evidence and questions

Source: [damage screen](../../app/src/main/java/com/juans/inspeccion/Interfaz/Inspeccion_tabs/p2.java), [rules/costs](../../app/src/main/java/com/juans/inspeccion/Mundo/DaniosManager.java), [labels](../../app/src/main/res/layout/nuevo_danio.xml), [album retention](../../app/src/main/java/com/juans/inspeccion/Mundo/Album.java). Confirm zero-cost tariff fallback, intended update/copy behavior, numeric bounds, rounding and total recalculation (ACCEPT-03). Implementation detail belongs in [architecture](../architecture/current-system.md).

## Step 02 regression evidence

DaniosManagerTest and DamageCostLookupTest now execute required/dimension/size rules, cost/tax and zero fallback, add/remove/copy and repeated recalculation. UI charge selection, update callback and video-only warning remain GATE-04-01 / ACCEPT-03. See [executed tests and precise gates](../testing/step-02-regression.md). Helper/request tests do not establish operator, full-screen, device or service acceptance.
