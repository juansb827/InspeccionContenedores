# Printing

Status: S/T; no physical receipt acceptance. Reviewed 2026-10-13, app 1.10 at `0a365c1`. Reviewer needed: printer/operator owner.

## Purpose and entry points

“Configurar Impresora” is available from settings/inspection. Select a paired Bluetooth device and optionally print a test image. After successful inspection save, choose “Imprimir” from “Guardó correctamente” / “Desea imprimir?”. A read-only document also exposes “Imprimir”. A normal new inspection hides the direct print menu until the post-save path.

| ID | Current visible behavior | Evidence |
| --- | --- | --- |
| PRINT-001 | Device selection uses “Seleccione la impresora”; no paired device shows “No hay impresoras vinculadas; vincule una en Ajustes de Bluetooth”. Selecting saves printer name/address for reuse. | S |
| PRINT-002 | Printing requires usable Bluetooth and required permissions. Denial, unsupported/off Bluetooth or revoked permission blocks transport/commands. Required permission or Bluetooth-enablement requests precede output; failure shows a message. | S/T |
| PRINT-003 | Missing printer configuration shows “No ha configurado ninguna impresora”. Failed connection reports “No se pudo conectar a la impresora; no se imprimió”. A failed command stops subsequent commands and disconnects. | S/T |
| PRINT-004 | Receipt output uses the inspection's document/direction, container, transport/inspector details and entry/exit receipt variant. Printing locks conflicting interaction; leaving during print shows “Espere a que termine la impresión”. | S; physical layout unverified |
| PRINT-005 | Declining the post-save prompt (“No, Gracias”) clears inspection state; declining the standalone prompt only disconnects. Physical printing success/confirmation and repeated printing need acceptance; successful command completion alone is not proof of a correct receipt. | S |

## Cancellation, failures and recovery

Printing is an explicit side effect, independent of remote save and media upload. An error midway may leave a partial receipt; no automatic replay is established. Before retrying, inspect test printer output to avoid duplicates. Receipt completion currently opens a confirmation dialog using shared dialog state; its exact displayed wording and subsequent form-clearing behavior need ACCEPT-05, not a guessed success claim.

## Persistence and side effects

Printer selection survives reopening. Test print consumes paper; receipts send commands to the selected device. Nothing in printer helper tests proves deployed printer model compatibility or paper formatting. Physical receipt acceptance requires a dedicated printer and sanitized inspection data.

## Acceptance examples

| ID | Given | When | Then | Verification |
| --- | --- | --- | --- | --- |
| PRINT-002 | Permission denied | Print requested | No transport opened or commands sent | T: PrinterPermissionTest/PrinterTransportTest |
| PRINT-003 | Failed second command | Receipt helper sends output | Later commands stopped; transport closed | T: PrinterTransportTest |
| PRINT-004 | Entry/exit test document | Physical print | Correct variant/values and legible receipt | S; ACCEPT-05 |

## Evidence and questions

Source: [printer setup](../../app/src/main/java/com/juans/inspeccion/Interfaz/ImpresoraActivity.java), [inspection print/prompt](../../app/src/main/java/com/juans/inspeccion/Interfaz/InspeccionActivity.java), [receipt composition](../../app/src/main/java/com/juans/inspeccion/Mundo/Recibos/CreadorRecibos.java), [permission tests](../../app/src/test/java/com/juans/inspeccion/Mundo/Recibos/PrinterPermissionTest.java), [transport tests](../../app/src/test/java/com/juans/inspeccion/Mundo/Recibos/PrinterTransportTest.java). Confirm printer fleet, entry/exit formats, confirmation wording, partial-output retry and form clearing in [acceptance](../testing/acceptance.md).
