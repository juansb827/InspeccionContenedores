# Searches and exports

Status: S/T helper evidence; no authenticated report execution. Reviewed 2026-10-13, app 1.10 at `0a365c1`. Reviewer needed: reporting operator.

## Purpose and prerequisites

Main-screen actions are “Buscar Documento”, “Modificar Documento”, “Stock detallado por Linea”, “Stock resumido” and “Turnos Registrados”. Searches/reports require business services; viewing reports also needs an installed Excel viewer.

| ID | Current visible behavior | Evidence |
| --- | --- | --- |
| SEARCH-001 | “Buscar Documento” asks “Buscar EIR” / “Ingrese el codigo del contenedor”; a code whose length is not 11 shows “Codigo invalido”. “Tipo de EIR” selects “ENTRADA” / “SALIDA”, opening read-only patio document view. This is a container-code search, not direct EIR-number entry. | S |
| SEARCH-002 | “Modificar Documento” uses “Modifica Guia” and the same container/direction selection; only today's document may be modified. Read-only view hides save/draft actions and allows printing, while modification hides draft-save/normal-print actions. | S |
| SEARCH-003 | “Stock detallado por Linea” asks for “LINEAS”; summarized stock and registered turnos use the business-service date, with turnos scoped to that date. Each produces an Excel report and opens a viewer. | S |
| SEARCH-004 | Report preparation shows “Un momento por favor...”. Export failure shows its error or “No se pudo generar el archivo”. No installed viewer shows “No hay una aplicación para abrir este archivo”. | S/T file-helper tests only |
| SEARCH-005 | Report replacement keeps the previous complete file if generating the replacement fails. Reports remain private to the app and are granted to a viewer for reading. | S/T |

## Cancellation, recovery and side effects

Back closes the report screen. Canceling a line-selection dialog does not establish a report result. Business-date loading failure and cancellation during background report generation have no accepted recovery contract; ACCEPT-06 must exercise them. There is no verified offline report workflow. Search/read-only viewing reads remote records; modification is the separate write path described in entry/exit documents.

## Persistence and boundaries

Generated Excel files can be overwritten by the same report type; external viewer behavior/retention is outside the app's verified contract. PDF generation/opening helpers exist and are tested with local fixtures, but the main-screen PDF demo is commented out. A PDF operator export is not a current reachable feature established by this inspection.

## Acceptance examples

| ID | Given | When | Then | Verification |
| --- | --- | --- | --- | --- |
| SEARCH-001 | 10-character container code | Search | “Codigo invalido” | S; ACCEPT-06 |
| SEARCH-003 | Sanitized line stock and service date | Detailed report | Excel for chosen line | S; ACCEPT-06 |
| SEARCH-005 | Existing complete report | Replacement fails | Previous bytes retained | T: FileConsumersTest |

## Evidence and questions

Source: [main search](../../app/src/main/java/com/juans/inspeccion/ui/access/MainActivity.java), [reports](../../app/src/main/java/com/juans/inspeccion/ui/search/ConsultasActivity.java), [file helpers](../../app/src/main/java/com/juans/inspeccion/ui/common/Varios.java), [viewer fallback](../../app/src/main/java/com/juans/inspeccion/platform/export/ExportFiles.java), [export tests](../../app/src/test/java/com/juans/inspeccion/platform/export/FileConsumersTest.java). Confirm report limits, expected columns, empty-result UX, viewers and date-failure behavior (ACCEPT-06). Technical report limit and file paths: [architecture](../architecture/current-system.md), [inventory](../architecture/storage-and-side-effects.md).
