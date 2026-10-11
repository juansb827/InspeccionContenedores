# Android migration

Compatibility begins at the last released commit, `d9c52d3`. Storage introduced
by unreleased commits is deliberately not imported. Application ID and the
release signing identity must stay the same for an in-place upgrade.

## Released serialization identifiers

The unmodified released model sources were compiled with `javac --release 8`
and inspected with `serialver`, without launching the application:

| Class | serialVersionUID |
| --- | --- |
| Album | 4164294007417506670 |
| Inspeccion | 7140579573600875404 |
| Danio | 779705593577192224 |
| DaniosManager | 6522051388807633138 |

The changed models explicitly retain these identifiers. Tests read serialized
draft and upload-queue fixtures generated using the released model sources.
The existing `listaPendientes` and `fotosPendientes` filenames are retained.
Corrupt stores fail visibly and cannot be replaced by empty stores.

## Storage and recovery

New captures are staged privately, processed without deleting originals, and
published into the primary shared photo library (`external_primary` on Android
10+; the synthetic aggregate volume is never used for insertion). Gallery photos survive routine form
clearing, successful uploads, and uninstall. Explicit photo deletion is the
only normal operation that removes a gallery photo.

Legacy import selects the released `Inspecciones` directory through the system
folder picker. Import keeps the source files, tracks copied references, and is
safe to repeat after interruption. Missing photos or revoked folder access do
not discard the draft. Import records must remain intact for retry. Verified references are merged into
the latest stored drafts and queue; importing cannot overwrite edits made while
copying or restore acknowledged upload entries.

No real camera, printer, production database, or FTP server is used by the JVM
tests. Device-only acceptance checks will be recorded after implementation.

## Capture and file consumers

Both photo buttons use `TakePicture` with request-specific URI grants. The capture
journal records the inspection, album and publication IDs before invoking the
external camera. A canceled or missing camera handler removes only its private
file. Recovery processes existing bytes without reopening the camera. Completed
association snapshots survive process death until saved state or draft storage
acknowledges them. Photo dialogs resolve albums by saved identifiers.

Excel and PDF replacements use atomic writes in `files/exports/`. The provider
exposes only `files/camera/` and `files/exports/` through the single application
authority. Viewer intents specify Excel/PDF MIME types and temporary read access.
The PDF generator reads `logo_centralpark.png` from assets; its dormant entry
point stays dormant. Uploads accept streams and remote names and acknowledge
queue entries after successful transfers; the background service stays inactive.

## Permissions and build

The build stays at AGP 8.10.1 / Gradle 8.11.1, compile SDK 34, target SDK 33,
minimum SDK 22, application ID `com.juans.inspeccion`, version 10 / 1.10.
AndroidX AppCompat 1.6.1 and Material 1.11.0 remain pinned. ExifInterface is
1.3.7 and DocumentFile is 1.0.1. Repositories are Google and Maven Central.
New Relic and EasyPermissions are removed. Legacy resource-ID and BuildConfig
compatibility settings remain enabled.

Capture uses an external camera app and does not declare CAMERA permission.
Legacy read/write storage permissions end at API 28; modern shared photos need
no broad media or all-files permission. On API 28 and below, both legacy storage
permissions are requested explicitly; a write-only grant is insufficient.
[Android 8.0 tightened permission-group grants](https://developer.android.com/about/versions/oreo/android-8.0-changes#permissions).
Legacy folder import uses persisted
user-selected read access. Bluetooth legacy permissions end at API 30.
CONNECT and SCAN are requested on API 31+, with `neverForLocation` on SCAN.
The bundled Godex SDK calls `cancelDiscovery()` while opening a connection,
which explains the SCAN requirement even for an already paired printer.
Selection, test printing and both receipt paths use the permission gate. The
transport checks permission and adapter state before each command and stops
on connection or transmission failure. Network permissions and cleartext
compatibility remain unchanged. No automatic uploads are enabled.

References: [shared media](https://developer.android.com/training/data-storage/shared/media),
[folder access](https://developer.android.com/training/data-storage/shared/documents-files),
[Bluetooth permissions](https://developer.android.com/develop/connectivity/bluetooth/bt-permissions).

## Lint baseline

Lint fails on new errors. Only seven unrelated diagnostics are baselined; each
source snippet was checked against both `d9c52d3` and `ac41db8`:

| Diagnostic | Existing code | Reason for deferral |
| --- | --- | --- |
| MissingSuperCall (2) | ConsultasActivity, FichaContenedorActivity | Legacy custom back navigation; unchanged by the migration. |
| SoonBlockedPrivateApi (2) | EditTextDialog cursor styling, LoginActivity overflow-menu reflection | Legacy cosmetic reflection, already inside exception handlers; a separate UI cleanup is needed. |
| AppCompatCustomView (3) | MyCheckBox, MyEditText, MyTextView | Existing widget base classes and theme behavior; changing these affects unrelated forms. |

No permission, storage, camera, export or upload diagnostic is baselined. Lint
warnings remain visible (660 warnings in the validation run). Adjacent indentation
issues and the restricted fragment inflater call were corrected. Inspection
back navigation uses the dispatcher so pending capture and printing guards remain
active without suppressing a lifecycle diagnostic.

## Validation

Validation ran locally using JBR 21.0.11 and the configured Android SDK:

```sh
JAVA_HOME=/home/juanb/.jdks/jbr-21.0.11 ./gradlew assembleDebug testDebugUnitTest lintDebug
JAVA_HOME=/home/juanb/.jdks/jbr-21.0.11 ./gradlew :app:dependencies --configuration debugRuntimeClasspath
```

The build and lint passed, and all 52 JVM tests passed. Tests cover released
serialization and persisted UUID upgrades, atomic replacement failure, UUID-based
draft updates, capture routing/recreation/cancellation/duplicate results/missing
handlers, distinct capture destinations, canceled follow-up captures, every EXIF
transform, corrupt images, sampling/no upscaling, failed publication and retry,
API 28/29 storage and denial, legacy-copy retry/missing or revoked sources,
remote naming, provider roots/authority, MIME types/missing viewers/readable PDF,
Bluetooth boundaries and denied/disabled/revoked access, connection/command
failure, and partial upload acknowledgement with retained gallery copies.
Folder-picker cancellation returns before accessing or changing persisted data;
a regression test verifies canceled imports leave both released files byte for byte
unchanged, without even rewriting their missing UUIDs. No app was installed or launched,
no physical camera or printer was accessed, and no production service was called.

The merged debug manifest was inspected: SDK 22/33, version 10/1.10, the expected
permission caps, no camera/phone-state/broad-media permissions, cleartext enabled,
and `com.juans.inspeccion.fileprovider`. AndroidX's internal signature permission
for non-exported dynamic receivers is expected. The runtime graph was inspected:
no New Relic or EasyPermissions; the requested library versions remain pinned.
Reports are generated under `app/build/reports/` and the debug APK under
`app/build/outputs/apk/debug/`.

## Release signing

Production release remains separate. The previous configuration inherited the
local debug keystore for every build type. Release builds now remain unsigned,
so this machine's debug identity cannot replace the deployed signing identity.
Use the original deployed keystore and verify its certificate before publishing.
No keystore or replacement production identity was created.

Read-only APK certificate inspection found these SHA-256 certificate fingerprints:

| Artifact | Version | Certificate SHA-256 |
| --- | --- | --- |
| checked-in app-release.apk | 7 / 1.6 | `005db5cda9d00b68f0c7701aa455ab0c1e4472e155d95f6be75f1ff122c2b4fd` |
| checked-in focus.apk | 4 / 1.3 | `69f5b555e2ba29d1837e9662576b9469cc80a4f2693996e2b291e5b322c5f848` |
| local debug build | 10 / 1.10 | `9244fca0beea7f01471b17853c23384484029ce77349c21c98940ac8227bad02` |

Neither older checked-in APK establishes the signing certificate of the deployed
version at `d9c52d3`; its release keystore is not configured in this repository.
The application ID is unchanged, but an in-place production upgrade still requires
that deployed key.

## Device-only checks (not performed)

Before production release, use test devices and non-production services to check:

- Android 5.1–9 storage denial/grant and publication into Pictures; Android 10+
  pending MediaStore writes, visibility in Gallery, and survival after uninstall.
- OEM camera URI grants, cancellation/empty output, rotation/mirroring, missing
  camera handlers, rotation/recreation and process death while capturing or
  publishing. Confirm recovery saves existing bytes without reopening the camera.
- Both photo buttons, uncommitted damage albums, repeated inspections of one
  container, gallery deletion confirmation, and retained gallery media after form
  clearing. Verify old drafts and queues import from the released folder, canceled
  folder selection preserves them, and revoked folder access can be granted again.
- Excel/PDF viewer grants with installed and missing viewers and replacement
  failure. Confirm the PDF logo is available with no Downloads directory.
- API 30/31+ paired-printer selection and all printing buttons with Bluetooth
  unsupported, off, denied or revoked, and failed connections. Inspect a real test
  receipt and verify no further commands follow an error.
- Stream uploads against a test FTP endpoint, interrupted connections and retry,
  durable progress and retained gallery copies. Confirm the dormant background
  service is still not started automatically.
- Sign with the original deployed certificate and perform an upgrade over the
  released app on a test device with drafts/photos, before any production rollout.
