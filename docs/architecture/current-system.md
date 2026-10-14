# Current architecture and integrations

Step-01 baseline evidence: S, reviewed 2026-10-13 against working-tree source at `0a365c1e8035f38fe13c358704fb6ab8e2a83e10`. Execution is recorded in [testing baseline](../testing/baseline.md). Proposed package/data boundaries in modernization steps 03–05 are not implemented here.

## Identity and build

| Item | Current source baseline |
| --- | --- |
| Structure | Java/XML; one `app` application module; Java source/target 8 |
| Identity | Application ID and namespace `com.juans.inspeccion`; versionCode 10, versionName 1.10 |
| Platform | min SDK 29, target SDK 33, compile SDK 34 |
| Toolchain | AGP 8.10.1, Gradle wrapper 8.11.1; JBR 21 used in this session |
| Signing | Debug signing for debug; release signing null/unsigned. Deployed certificate/key owner and distribution method remain unverified. |
| Tests | JUnit 4.13.2, Robolectric 4.14.1; custom GalleryInstrumentation runner delegates camera/video suites |
| Lint | `abortOnError true`, existing `lint-baseline.xml` filters errors; successful lint is not zero findings |

Sources: [app build](../../app/build.gradle), [root build](../../build.gradle), [wrapper](../../gradle/wrapper/gradle-wrapper.properties), [manifest](../../app/src/main/AndroidManifest.xml), [test resource override](../../app/legacy-robolectric.gradle).

Pinned runtime dependencies include AppCompat 1.6.1, ViewPager 1.0.0, RecyclerView 1.3.2, ViewPager2 1.1.0, Glide 4.16.0, PhotoView 2.3.0, Loader 1.1.0, Material 1.11.0, ExifInterface 1.3.7, DocumentFile 1.0.1, CameraX core/camera2/lifecycle/view/video 1.4.2 and Media3 player/UI 1.4.1. JitPack is restricted to PhotoView's group. Bundled JARs include jTDS 1.2.7, iText 5.0.5, jxl, POI 3.10.1, EZioAD/Godex, commons-net 3.3, commons-io 2.4 and wrapped Apache HTTP. This is an inventory, not a recommendation to upgrade.

## Entry points and modes

Launcher is `Interfaz.LoginActivity`. Registered activities include Main, Settings, Inspeccion, FichaContenedor, Consultas, Pendientes, Impresora and Visualizar. The latter displays arbitrary provided content and has no established primary main-screen entry. Residual camera/turnos/print layouts/menu names do not prove corresponding activities/workflows exist. The [debug manifest](../../app/src/debug/AndroidManifest.xml) adds only the non-exported GalleryDeviceHost fixture; never use it to assert full business-screen acceptance.

| Entry | Mode/context | Primary document |
| --- | --- | --- |
| Main “Linea Naviera” → “Entrada” / “Salida” | `EP`, patio inspection; information + damages | [Entry](../features/entry-inspections.md), [exit](../features/exit-inspections.md) |
| Main “Empresa de Transporte” → “Entrada” / “Salida” | `ET`; information only | Entry/exit |
| “Buscar Documento” → container/direction | EP read-only; save/draft hidden; print available | [Search](../features/searches-and-exports.md) |
| “Modificar Documento” → container/direction | EP modification, same business-service day; draft/normal print hidden | Entry/exit |
| “Turnos Sin Guardar” → row | Resumed local draft/document type | [Drafts](../features/pending-inspections.md) |
| Container info → create/edit | Writes a container independently of inspection save | [Containers](../features/containers-and-bookings.md) |
| Inspection general/damage camera/gallery | Media admission depends on album, direction, read-only/action and busy flags | [Media](../features/camera-and-media.md) |
| Settings/main/report/printer | Connection/reference configuration, reports, Bluetooth receipt | [Access](../features/access-and-settings.md), [printing](../features/printing.md) |

The main-screen navigation sets mode/document extras and static action state. `InspeccionActivity` hosts `p1` / `p2`, receives serialized draft/bundle state, manages save/print and capture recovery. `p1` loads turnos/container/bookings and binds information; `p2` manages damage editing/cost lookup. `soloLectura`, `tipoAccion`, `usaTurno` and login inspector state have static ownership. Turno use is initialized from cached company configuration and copied into the inspection; it is not an independent mode choice on the main screen. They are not a verified durable session model.

## Packages and current boundaries

| Package | Current responsibility / coupling |
| --- | --- |
| `Interfaz`, `Interfaz.Inspeccion_tabs`, `Dialogs` | Activities/fragments, form callbacks, lifecycle, async saves/loads and dialogs; UI participates in database operations |
| `Interfaz.Camera`, `Interfaz.Gallery` | Capture/gallery UI, retained models, identity resolution, playback and deletion |
| `CustomView` | Widgets carry source/destination SQL table/field metadata, required status and values |
| `Mundo`, `Mundo.Data` | Serializable inspection/album/damage models, rule calculations, SQL generation/data loaders, caches, drafts and queue |
| `Mundo.Storage` | Atomic serialized files, public media adapters, capture/publication recovery, legacy import, JSON media index, export files |
| `Mundo.Recibos`, `Mundo.Util` | Receipt/PDF command generation, printer transport/permissions, FTP helpers |
| `Other`, root utilities/exceptions | Dormant service/receiver/observer, date/number/file helpers and exceptions |

There is no repository interface separating business logic from forms/SQL across the application. `InspeccionDataSource` takes `CustomView` collections and Android resources; `Formularios` traverses a particular table/row hierarchy. Hidden `GoneView` fields also participate. Field binding and date conversion must be characterized before changing hierarchy or packages. Damage layouts use spans/weighted vertical space; no dp/font-scale measurement or truncation diagnosis was performed in step 01.

## Remote operation inventory

All active business traffic is direct SQL Server via the shared `DAO` connection and bundled jTDS. A backend API is excluded by [ADR 001](../decisions/001-modernization-scope.md). Table/column/function identifiers are in [database resources](../../app/src/main/res/values/database.xml); no endpoint or credential is needed to understand these operations.

| Operation | Current callers/boundary | Effect and unresolved integration checks |
| --- | --- | --- |
| Inspector lookup | Login → UsuarioDataSource | Read inspector identity/enabled status; no live test |
| Connection test | Settings → DAO | Opens/tests configured connection; settings save is a separate private-file write |
| Reference metadata/list/company/date load | Main InfoEmpresa, ColumnasTablas, Listas, Consultas/DAO | Reads SQL metadata, catalogs, company/logo and service date; persists rebuildable caches |
| Turno/header/container lookup | p1 → InspeccionDataSource.read/readInfoCntr/readTurnosPendientes | Reads attended/direction/yard state, client/agent/activity and pending turnos |
| Booking/tariff lookup | p1/p2 → InspeccionDataSource/Consultas/DaniosManager | Reads membership/balance, repair rates, labor and IVA; zero fallback on rate failure |
| New inspection submission | InspeccionActivity → InspeccionDataSource.insert | With no turno, generate consecutive number/agent; insert header and entry-only details; exit uses originating container document |
| Existing document view/modify | InspeccionDataSource.read/re_insert | Select entry/or exit number from container; modification is same-day; delete/insert header and entry-only replacement details |
| Container create/edit | FichaContenedor → insertContenedor/updateContenedor | Inserts/updates container; code change may update associated turno in a combined operation |
| Stock/day reports | ConsultasActivity → DAO | Read server-date scoped reports; export `.xls` with result limit 1000 |
| Receipt and PDF helpers | CreadorRecibos/CreadorFacturasPDF | Use current inspection and cached company data; Bluetooth output / private export. PDF helper has no established active main-screen path. |
| Photo FTP queue | FotosPendientes/FTPUpload/SimpleService | Dormant app entry paths; helper-only fake tests. Never enable automatically. |

`DAO` concatenates query values and type metadata into SQL, owns a shared `Connection`, relaxes StrictMode and logs a connection URL containing credentials and SQL containing data. Do not copy those logs/settings into documentation or fixtures. Transaction helpers generate `BEGIN TRAN`, statements plus `IF @@ERROR ... ROLLBACK ... return`, and `COMMIT TRAN`; inspection save interprets no thrown error as success. No step-01 run proves server atomicity, affected rows, concurrency, collation or uncertain-commit handling. These require test SQL Server in step 05; step 02 should create fake submission boundaries.

## Media and other external interfaces

CameraX binds rear photo/video capture to lifecycle. Retained capture models publish with MediaStore, resolving completion against current inspection/album identities. Photo processing applies image orientation, limits output width to 1280 pixels and encodes JPEG quality 90. Videos use audio and fixed duration/size limits; Media3 handles local playback. `GalleryAlbums.canDelete` is also capture admission: normal editable general albums permit it in either direction; damage albums exclude Salida. Deletion journals distinguish byte deletion from metadata completion.

FileProvider exposes only private `camera/` and `exports/`; Excel/PDF open intents grant read access. SAF folder import persists a read grant and keeps original files. Bluetooth permissions gate paired-device selection/transport (Android 12+ requests CONNECT/SCAN); print uses its own executor and disconnects on errors. Internet/network/Wi-Fi, camera, microphone and Bluetooth permissions are declared; pre-29 storage permission branches remain historical test targets. Manifest permits cleartext and app backup. Those declarations do not establish network or backup acceptance.

`enviarFotos()` constructs an intent without starting the service; main demo service start and receiver starts are commented. SimpleService's FTP parameters are placeholders. Helper tests cannot prove a functioning upload feature.

## Findings to characterize before refactoring

- No-turno initializer is called when `usaTurno` is false but returns when false; the static field defaults to false but company configuration can set it at inspection creation. Reachable operator behavior must be resolved with sanitized inspection data (ACCEPT-02), not inferred from method names/comments.
- Damage add does not repeat full charge-selection validation; update-button callback is empty; copying selected damage omits its album. Missing tariffs/connection exceptions zero costs.
- `DaniosManager.recalcularValorTotal()` adds to existing `valorTotal` without resetting it. Repeated load/recalculation and rounding/formatting need characterization and separate correction decisions.
- Container field handlers mostly enforce code-part lengths rather than digits/checksum. Required-field metadata is dynamic.
- Photo-only unsaved-damage warning ignores video-only albums. Association loss is distinct from deletion of public bytes.
- Report callers retain an older Excel-viewer error catch, but ExportFiles.open consumes missing-viewer exceptions and shows its own generic message. Trace through helpers rather than treating every catch message as reachable.
- Submission has no verified duplicate-tap/uncertain-commit coordinator. Receipt completion constructs a dialog relying on shared static dialog state; exact completion UX is unverified.

These are S findings, not operator-approved defects or fixes. Step 01 changes documentation only. See the [acceptance register](../testing/acceptance.md) and [storage audit](storage-and-side-effects.md) before choosing step-02 tests.

## Step 02 narrow test seams

Step 02 introduces protected external-operation accessors on InspeccionDataSource, a subclassable DAO constructor and metadata accessor, extracted DamageCostLookup remote reads, InspectionRules from p1 and InspectionSubmission save/local-cleanup work used by the activity. Default SQL/metadata/date sources, calculation/validation quirks, serialization, layouts, prompts and async ownership remain. This is not a completed repository/business architecture; static Consultas/Contenedores and form coupling still exist. The new StorageFormatProbe is unwired, with no writer/reset/quarantine/retirement changes. [Tests, actual results and limitations](../testing/step-02-regression.md) define the supported seams and gates before broader changes. The existing serialized model method signatures were preserved and released deserialization cases still pass.
