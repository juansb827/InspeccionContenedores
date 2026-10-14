# Access and settings

Status: S/T; no operator walkthrough. Reviewed 2026-10-13, app 1.10 at `0a365c1`. Reviewer needed: operator and deployment owner.

## Purpose and entry points

An inspector opens the login screen, supplies a numeric inspector code and uses configured business services. “Configuración” is available from login and the main screen. Connection settings require “Servidor”, “Base de Datos”, “Usuario” and “Contraseña”; “Instancia del Servidor” is optional. No credentials are documented here.

## Behavior and validation

| ID | Current visible behavior | Evidence |
| --- | --- | --- |
| ACCESS-001 | Missing saved connection settings shows the login warning and disables the login button. Non-numeric code shows “Ingrese un codigo valido”. | S |
| ACCESS-002 | Login distinguishes “Error en conexion con base de datos”, “Inspector no encontrado” and “El inspector se encuentra deshabilitado”. An enabled inspector receives “Bienvenido ” followed by the name and reaches the main screen. | S |
| ACCESS-003 | “Probar Conexión” rejects empty required settings with “Complete el formulario”; success shows “Conexión Exitosa”. Failure offers “Mostrar Error”. “Guardar Configuracion” can save after the failed-connection confirmation using “Si” / “No”. | S |
| ACCESS-004 | “Actualizar Diccionarios”, “Actualizar Datos Empresa” and “Actualizar Listas” invalidate their respective local reference information. Returning to the main screen triggers missing-data synchronization. | S |
| ACCESS-005 | “Importar fotos antiguas” asks for the released Inspecciones folder, preserves originals, and permits retry of partial import. Canceling selection leaves saved references unchanged; denied/revoked access preserves data. | S/T |
| ACCESS-006 | “Configurar Impresora” opens printer setup. “Ayuda” currently has no visible help action in login/main handlers. | S |

## Cancellation, failures and recovery

Login temporarily disables its button while looking up the inspector. The main screen displays “Sincronizando” or “Error de Conexion” while loading reference information. Connectivity notifications/retry intent do not establish a working offline login. Login session survival after process death is unverified. A successful test of settings is not proof that later writes will succeed; repeated testing after edits and save-button timing need acceptance review.

## Persistence and side effects

Saved settings are reused on reopening. Testing and synchronization contact business services; reference refresh removes caches, not inspection records. Import creates public photo copies and updates saved associations. Its current dependency on readable old drafts/queue will need review at the future retirement cutover; the import control is not permission to restore abandoned drafts.

## Acceptance examples

| ID | Given | When | Then | Verification |
| --- | --- | --- | --- | --- |
| ACCESS-002 | A disabled test inspector | Login is attempted | Disabled message; no main screen | S; manual gap ACCEPT-01 |
| ACCESS-005 | A sanitized old-photo fixture | Folder selection is canceled | Original references/bytes remain | T: LegacyPhotoImporterTest; E status in baseline |
| ACCESS-003 | A failed test connection | Operator chooses “No” on save confirmation | Saved settings remain unchanged | S; manual gap ACCEPT-01 |

## Evidence and open questions

Source: [login](../../app/src/main/java/com/juans/inspeccion/ui/access/LoginActivity.java), [main](../../app/src/main/java/com/juans/inspeccion/ui/access/MainActivity.java), [settings](../../app/src/main/java/com/juans/inspeccion/ui/settings/SettingsActivity.java), [labels](../../app/src/main/res/values/strings.xml), [import tests](../../app/src/test/java/com/juans/inspeccion/platform/media/LegacyPhotoImporterTest.java). Confirm inspector authorization expectations, deployment setup, real retry behavior and session restoration in [acceptance](../testing/acceptance.md). Technical details: [architecture](../architecture/current-system.md).
