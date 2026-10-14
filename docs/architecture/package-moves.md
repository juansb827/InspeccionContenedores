# Package move inventory and deliberate exceptions

Inventory built from the working tree before moves, including step-01/02 uncommitted additions. [Step-03 evidence](../testing/step-03-cutover-and-moves.md) records gate ordering and execution. These destinations improve ownership/naming; they do not establish step-04 dependency boundaries.

## Production move manifest

All names are relative to `com.juans.inspeccion`. Grouped compile/focused-test milestones separate UI, media, and data/platform moves. An entry bridge means the original class remains a thin subclass for Android component or fragment restoration; source callers use the new implementation. Persisted draft compatibility has no bridges.

| Original identity | Implementation destination | Review group / compatibility |
| --- | --- | --- |
| `ConnectionException` | `data.remote.sqlserver.ConnectionException` | data |
| `CustomView.MyCheckBox` | `ui.common.widgets.MyCheckBox` | ui |
| `CustomView.MyTextView` | `ui.common.widgets.MyTextView` | ui |
| `CustomView.SlidingTabLayout` | `ui.common.widgets.SlidingTabLayout` | ui |
| `CustomView.SlidingTabStrip` | `ui.common.widgets.SlidingTabStrip` | ui |
| `DataBaseException` | `data.remote.sqlserver.DataBaseException` | data |
| `DiccionariosException` | `data.remote.sqlserver.DiccionariosException` | data |
| `Interfaz.ActivityHelper` | `ui.common.ActivityHelper` | ui |
| `Interfaz.Camera.CameraCaptureFragment` | `ui.media.camera.CameraCaptureFragment` | media; stable entry bridge |
| `Interfaz.Camera.CameraCaptureModel` | `ui.media.camera.CameraCaptureModel` | media |
| `Interfaz.Camera.CameraHost` | `ui.media.camera.CameraHost` | media |
| `Interfaz.ConsultasActivity` | `ui.search.ConsultasActivity` | ui; stable entry bridge |
| `Interfaz.CustomAdapters.MyPagerAdapter` | `ui.inspection.MyPagerAdapter` | ui |
| `Interfaz.Dialogs.BigProgressDialog` | `ui.common.dialogs.BigProgressDialog` | ui |
| `Interfaz.Dialogs.BusquedaDialog` | `ui.search.BusquedaDialog` | ui; stable entry bridge |
| `Interfaz.Dialogs.CompletarCampoDialog` | `ui.common.dialogs.CompletarCampoDialog` | ui; stable entry bridge |
| `Interfaz.Dialogs.ConsultaCortaDialog` | `ui.search.ConsultaCortaDialog` | ui; stable entry bridge |
| `Interfaz.Dialogs.CustomFragment` | `ui.common.dialogs.CustomFragment` | ui |
| `Interfaz.Dialogs.EditTextDialog` | `ui.common.dialogs.EditTextDialog` | ui; stable entry bridge |
| `Interfaz.Dialogs.FechaDialog` | `ui.common.dialogs.FechaDialog` | ui |
| `Interfaz.Dialogs.ListViewDialog` | `ui.common.dialogs.ListViewDialog` | ui; stable entry bridge |
| `Interfaz.Dialogs.MyProgressDialog` | `ui.common.dialogs.MyProgressDialog` | ui |
| `Interfaz.Dialogs.SimpleDialog` | `ui.common.dialogs.SimpleDialog` | ui; stable entry bridge |
| `Interfaz.Dialogs.YesNoDialog` | `ui.common.dialogs.YesNoDialog` | ui; stable entry bridge |
| `Interfaz.FichaContenedorActivity` | `ui.containers.FichaContenedorActivity` | ui; stable entry bridge |
| `Interfaz.Gallery.GalleryAdapter` | `ui.media.gallery.GalleryAdapter` | media |
| `Interfaz.Gallery.GalleryAlbums` | `ui.media.gallery.GalleryAlbums` | media |
| `Interfaz.Gallery.GalleryHost` | `ui.media.gallery.GalleryHost` | media |
| `Interfaz.Gallery.PhotoGalleryFragment` | `ui.media.gallery.PhotoGalleryFragment` | media; stable entry bridge |
| `Interfaz.ImpresoraActivity` | `ui.printing.ImpresoraActivity` | ui; stable entry bridge |
| `Interfaz.InspeccionActivity` | `ui.inspection.InspeccionActivity` | ui; stable entry bridge |
| `Interfaz.InspeccionFragment` | `ui.inspection.InspeccionFragment` | ui |
| `Interfaz.Inspeccion_tabs.p1` | `ui.inspection.InspectionInformationFragment` | ui; stable entry bridge |
| `Interfaz.Inspeccion_tabs.p2` | `ui.inspection.InspectionDamageFragment` | ui; stable entry bridge |
| `Interfaz.LoginActivity` | `ui.access.LoginActivity` | ui; stable entry bridge |
| `Interfaz.MainActivity` | `ui.access.MainActivity` | ui; stable entry bridge |
| `Interfaz.PendientesActivity` | `ui.pending.PendientesActivity` | ui; stable entry bridge |
| `Interfaz.PrinterPermissionGate` | `ui.printing.PrinterPermissionGate` | ui |
| `Interfaz.SettingsActivity` | `ui.settings.SettingsActivity` | ui; stable entry bridge |
| `Interfaz.UIEffects` | `ui.common.UIEffects` | ui |
| `Interfaz.VisualizarActivity` | `ui.search.VisualizarActivity` | ui; stable entry bridge |
| `InvalidOperationException` | `data.remote.sqlserver.InvalidOperationException` | data |
| `Mundo.ColumnasTablas` | `data.local.cache.ColumnasTablas` | data |
| `Mundo.Comando` | `platform.printing.Comando` | data |
| `Mundo.Consultas` | `data.remote.sqlserver.Consultas` | data |
| `Mundo.Contenedores` | `data.remote.sqlserver.Contenedores` | data |
| `Mundo.DAO` | `data.remote.sqlserver.DAO` | data |
| `Mundo.DamageCostLookup` | `data.remote.sqlserver.DamageCostLookup` | data |
| `Mundo.Data.DataLoader` | `ui.common.DataLoader` | data |
| `Mundo.Data.DataSource` | `data.remote.sqlserver.DataSource` | data |
| `Mundo.Data.InspeccionDataSource` | `data.remote.sqlserver.InspeccionDataSource` | data |
| `Mundo.Data.InspectionSubmission` | `ui.inspection.InspectionSubmission` | data |
| `Mundo.Data.UsuarioDataSource` | `data.remote.sqlserver.UsuarioDataSource` | data |
| `Mundo.Formularios` | `ui.common.Formularios` | data |
| `Mundo.FotosPendientes` | `data.local.FotosPendientes` | data |
| `Mundo.InspectionRules` | `domain.rules.InspectionRules` | data |
| `Mundo.Listas` | `data.local.cache.Listas` | data |
| `Mundo.MyCameraHelper` | `platform.media.MyCameraHelper` | media |
| `Mundo.Pendientes` | `data.local.Pendientes` | data |
| `Mundo.Recibos.ComandosImpresora` | `platform.printing.ComandosImpresora` | data |
| `Mundo.Recibos.CreadorFacturasPDF` | `platform.export.CreadorFacturasPDF` | data |
| `Mundo.Recibos.CreadorRecibos` | `ui.printing.CreadorRecibos` | data |
| `Mundo.Recibos.Impresora` | `platform.printing.Impresora` | data |
| `Mundo.Recibos.PrinterAccess` | `platform.printing.PrinterAccess` | data |
| `Mundo.Recibos.RecibosTest` | `ui.printing.RecibosTest` | data |
| `Mundo.Storage.AtomicObjects` | `data.local.AtomicObjects` | data |
| `Mundo.Storage.ExportFiles` | `platform.export.ExportFiles` | media |
| `Mundo.Storage.ImageProcessor` | `platform.media.ImageProcessor` | media |
| `Mundo.Storage.LegacyPhotoImporter` | `platform.media.LegacyPhotoImporter` | media |
| `Mundo.Storage.LocalMediaIndex` | `platform.media.LocalMediaIndex` | media |
| `Mundo.Storage.PhotoNames` | `platform.media.PhotoNames` | media |
| `Mundo.Storage.PhotoStore` | `platform.media.PhotoStore` | media |
| `Mundo.Storage.PrivateTakePicture` | `platform.media.PrivateTakePicture` | media |
| `Mundo.Storage.StorageFormatProbe` | `data.local.StorageFormatProbe` | data |
| `Mundo.Storage.StoredInspections` | `data.local.StoredInspections` | data |
| `Mundo.Storage.VideoStore` | `platform.media.VideoStore` | media |
| `Mundo.Threadinho` | `data.remote.sqlserver.Threadinho` | data |
| `Mundo.Usuario` | `domain.model.Usuario` | data |
| `Mundo.Util.FTPUpload` | `platform.media.FTPUpload` | media |
| `Mundo.Util.FTPUtil` | `platform.media.FTPUtil` | media |
| `Other.BroadcastObserver` | `app.BroadcastObserver` | data |
| `Other.NetWorkChangeReceiver` | `app.NetWorkChangeReceiver` | data; stable entry bridge |
| `Other.SimpleService` | `platform.media.SimpleService` | data; stable entry bridge |
| `Varios` | `ui.common.Varios` | data |

## Non-import references and Android paths

- Original activity, receiver and retired-service manifest names remain registered. New implementations have explicit internal registrations; the original launcher intent-filter stays on `Interfaz.LoginActivity`. New internal activity intents use the registered implementations. Application ID/namespace/version, provider authority `${applicationId}.fileprovider`, resource IDs and signing configuration are unchanged.
- `p1` → `InspectionInformationFragment`, `p2` → `InspectionDamageFragment`. Original fragment names and moved dialog/camera/gallery fragment names remain no-argument subclass bridges for saved FragmentManager identities. New factories create the new classes. Serialized inspection/editing/damage values remain stable exceptions below.
- XML custom-view names for moved nonserialized widgets point to `ui.common.widgets`; IDs, attributes, nesting and layout behavior are retained. `MyEditText`/`GoneView` keep their old identity because actual loader bundles serialize widget collections. No old nonserialized widget bridges are needed for hierarchy-state restoration.
- Debug-only `GalleryDeviceHost` and local gallery/camera instrumentation implementations move to the media UI packages. The original configured `Interfaz.Gallery.GalleryInstrumentation` runner remains a stable bridge; no build dependency or runner selection changes are needed. Debug manifest registers the implementation and retains its original fixture entry point. Storage suite remains `-e suite storage`.
- Reflection on the gallery confirmation and camera elapsed/dialog fields targets implementation classes. JDBC driver lookup is unchanged. Historical platform reflection in Login/EditTextDialog remains characterized by the same lint baseline issues. DraftCodec's explicit adapter reflects private IDs/maps/list/total fields of retained legacy models; schema bytes never contain those Java names.
- JVM suites move with their subjects where package-private access permits. The retained serialized deletion-model tests and snapshot tests remain beside those exceptions; broad storage/file tests cover multiple adapters. SQL recording transport remains a test-only collaborator, made accessible across the submission/request suite packages without changing generated SQL.
- The lint baseline relocates existing file paths only. It retains the same seven issue records, messages and source-line identities; it is not regenerated or expanded.

## Active serialization / legacy implementation exceptions

| Retained identity | Concrete reason / destination when its contract is ready |
| --- | --- |
| `Mundo.Inspeccion`, `Album`, `Danio`, `DaniosManager` | Active photo/video snapshot graphs and Android extras/damage editing still encode these identities. Inspeccion/Album have fixed UIDs; Danio/manager use implicit UIDs. Keep names/signatures rather than lose current recovery. DaniosManager also mixes rules, Android resources and SQL. Step 04 isolates plain form/business state; a later explicit journal/Android-state migration can move models to `domain.model` and rules separately. They do **not** open abandoned draft collections. |
| `Mundo.FilaEnConsulta` | Serialized reference-catalog cache values; moving only their loader would invalidate otherwise preserved caches. Future typed reference-data mapping/cache decision precedes a move into `data.mapping`/model. |
| `CustomView.MyEditText`, `GoneView`, `CustomView` | Loader Bundle Serializable widget graphs and implicit UID/interface identity. Step 04 replaces widget transport with plain form inputs before moving these to `ui.common.widgets`. The interface remains the common bridge for moved CheckBox widgets. |
| `Mundo.Storage.CaptureCoordinator`, `VideoCaptureCoordinator` and nested Session | New `-v1` journals retain snapshot class identity; pre-cutover namespace is isolated. Future explicit snapshot schema precedes `platform.media` move. No stale old journal reads. |
| `Mundo.Storage.MediaPhotoStore`, `MediaVideoStore` and nested Publication | Independent publication-map values use their nested class names, enabling reuse of public rows after failure. Preserve names to prevent duplicate publication. Migrate only with a tested map contract, then move to `platform.media`. |
| `Interfaz.Gallery.GalleryDeletionModel`, `GalleryMediaItem.Kind` (and containing class) | Current deletion-result journal encodes Result and Kind. Preserve metadata retry/no-second-delete guarantees. A tested explicit result schema enables `ui.media.gallery`/platform split. |
| Original Android entry bridges | Released task/component and FragmentManager class identities; implementations are in lowercase responsibility packages. These are public/platform compatibility, not old-draft compatibility. |

## Remaining dependency cycles: step 04 input

`ui` still invokes SQL/caches directly; SQL data sources still accept CustomView/Resources; shared form helpers still know SQL metadata and screen callbacks. `domain.rules.InspectionRules` still refers to UI direction/document constants and Android Resources. Damage calculations remain tied to resource identifiers and tariff transport. DraftCodec temporarily maps private legacy state through reflection to avoid changing implicit serialization UIDs or accumulating totals. Media hosts/coordinators still carry legacy model graphs. Receipt formatting still reads UI/resource/cache state.

The package map does not claim purity. No empty repository/use-case/application scaffolding, backend, Room store, layout redesign or SQL rewrite was introduced. See [step 04](../modernization/04-data-and-business-boundaries.md) for the authorized next boundary work.
