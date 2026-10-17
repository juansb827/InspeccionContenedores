# Local storage, media, recovery and side effects

Current photo-path update (2026-10-17, MEDIA-002): newly captured inspection JPEGs use `Pictures/Inspecciones/<direction>_<EP/ET>_<document>_<container>/`, with `temp` until the document number is available. The existing submission-renaming path moves only associated photo URIs into their final readable folder with collision protection. There is no UUID-folder lookup/fallback or bulk migration. Unrecognized import filenames use the public `Pictures/Inspecciones/` root. Existing associated URIs can still be renamed through the normal submission path. Video locations, internal IDs, draft schema and publication/recovery identities are unchanged. [Naming behavior and scoped evidence](../testing/photo-naming.md) supersedes only the photo-location claims in the historical inventory below.

Evidence: S/T at `0a365c1`, reviewed 2026-10-13. No device filesystem/customer records were inspected. Paths below are logical app-relative/public paths, not machine setup paths. **The original inventory below is the pre-cutover baseline; the step-03 current inventory at the end supersedes its draft/queue/journal paths.** [ADR 002](../decisions/002-storage-retirement.md) and the [storage policy](../modernization/storage-version-policy.md) define the implemented step-03 cutover.

## Durable inventory

| Store / location | Current format, readers/writers | Retention/recovery and future classification |
| --- | --- | --- |
| Private `listaPendientes` | Serialized `ArrayList<Inspeccion>`; StoredInspections via Pendientes, inspection save/recovery, draft list, Settings and legacy importer | Active drafts now; IDs may be assigned and rewritten on read. Read before replacement; unreadable data preserved. **P: retire old format**, use a separate explicit current namespace; no old importer required. |
| Private `fotosPendientes` | Same collection encoding, upload-oriented inspection snapshots; StoredInspections via FotosPendientes, Settings and importer | Dormant queue; helper acknowledgements persist. **P: retire without replay**; no replacement upload work. |
| Atomic-file companions | AtomicObjects/AtomicFile recognizes `.bak` for reads; atomic writes may leave `.new` staging files | Inventory source and backup before future classification/cutover; never treat backups/journals as disposable because drafts are retired. Crash stages need tests. |
| Private `connectionSettings` | Serialized string map, DAO load/save | Connection endpoint/database/user/password/optional instance. Delete/recreate non-atomic writer; not draft retirement scope. Sensitive; never use real values in fixtures/logs/docs. |
| Preferences `printerSettings` | Android private SharedPreferences; Impresora load/save | Printer name/address persist; preserve/reselect as needed. Not a serialized inspection. |
| Private `<table-name>.xml` | Despite extension, serialized column-type maps, ColumnasTablas | SQL metadata caches; rebuild from business service; refresh deletes files. Not XML parsing or legacy draft data. |
| Private catalog files | Serialized objects keyed by `Listas` / `ListasNoInspeccion` resource arrays; Listas load/save | States, sequences/locations, components, damage/repair methods, lines/ships, ingreso, ports, types/materials/sizes; logical-use list built in code. Rebuildable caches, not draft retirement. |
| Private `infoEmpresa.hue`, `logoEmpresa.jpg` | Serialized company map and image bytes; ColumnasTablas load/save | Company/receipt/reference cache, may include integration settings; refresh deletes both. Not user media. Writers use append after deleting existing files; cache error behavior differs from protected drafts. |
| Private `camera/<capture-id>.jpg`, `processed-*.jpg`, `camera/<id>.mp4` | Photo/video stores create private capture bytes; photo processor uses scratch JPEG | Created only on shutter/record; kept through recoverable failure; private bytes removed after completion/discard. No blanket cleanup before journal audit. |
| Private `pending-photo-capture` | Atomic serialized CaptureCoordinator.Session with `Inspeccion`/`Album` snapshot, IDs, path, display/remote names, state and published URI | May restore uncommitted inspection/album. Completed snapshot remains until acknowledgement; discard retains public copy. **P: separately audit model compatibility and isolate unresolved legacy operations visibly.** |
| Private `pending-video-capture` | Atomic serialized VideoCaptureCoordinator.Session with inspection/album snapshots, IDs, path, finalized/interrupted/discard/finished state, reference/duration | Restores context; validates existing bytes before publication; not interchangeable with photo/draft data. **P: preserve/isolate; prevent stale replay before package moves.** |
| Private `photo-publications`, `video-publications` | Separate atomic serialized maps of capture identity to nested Publication URI/completion objects; MediaPhotoStore/MediaVideoStore | Reuse reserved/completed public row on retry; independent of drafts but tied to nested Java class names. **P: preserve/migrate bookkeeping to prevent duplicate publication/deletion.** |
| Private `pending-media-deletion` | Atomic serialized GalleryDeletionModel.Result: IDs, reference, kind/duration, busy/bytesDeleted/error; nested enum type | Before mutation and after byte deletion; metadata retry must not delete twice. Unknown journal blocks deletion. Does not contain full inspection snapshot but does depend on class/enum identity. Audit independently. |
| Private `legacy-photo-imports` | Atomic serialized string mapping; LegacyPhotoImporter | Old-reference to public-copy mapping, idempotent import; source folder SAF grant persists separately. Preserve original media and mapping; current importer reads both old stores, so review at cutover. |
| Private `local-media-index.json` | Atomic JSON `version: 1`, `albums` object, inspection/album ID keys and reference/mime/kind/duration rows; LocalMediaIndex | Reconciles photo order/references from albums and preserves video rows, including unavailable clips. Unknown version/malformed data errors preserve file. Independent of serialized draft fields; **preserve**, not queue retirement. Current errors are not the future draft classification API. |
| Public `Pictures/Inspecciones/<inspection-id>/` | JPEG MediaStore rows/bytes (supported Android 29+) | Preserve across form/damage/draft clearing. Explicit gallery confirmation deletes bytes. Local names can change after submission; URI stays stable and remote names are separate. |
| Public `Movies/Inspecciones/<inspection-id>/` | MP4 MediaStore rows/bytes | Same preservation; videos stay local and outside photo counters/names/FTP. Discard private new recording never authorizes deleting an earlier public row. |
| Historical public `/Inspecciones` references | Serialized old photo paths; operator-selected SAF source via legacy importer | Original bytes retained on copy. A path/reference does not prove exact producing app version. Associations only in retired drafts need not be reconstructed or guessed. |
| Private `exports/<safe-name>` | Atomic XLS/PDF bytes; ExportFiles, DAO, PDF helper | Rebuildable documents; failing replacement preserves last complete file; FileProvider exposes read access to viewers. |
| Activity/fragment saved bundles | Serialized inspection/editing album and form state plus view positions | Android-managed recreation state, not a new versioned durable draft store. Pending capture snapshots provide separate recovery. Package moves must audit bundle and Android entry-point identities too. |

No Room database is currently implemented. The `CREATE_COMMAND` example in InspeccionDataSource does not establish an active SQLite draft store.

## Current read/error behavior versus planned contract

Current AtomicObjects uses Java object serialization and AtomicFile; missing file/backup returns null. StoredInspections checks collection/item types, initializes IDs and wraps I/O, missing-class and runtime errors in a generic preserving StorageException. Its current caller distinguishes unavailable drafts from an empty UI but **does not classify retired/corrupt/unknown/newer/I/O outcomes separately**. Inspeccion/Album have pinned released serialVersionUIDs; other models and nested journal types also matter. Tests currently require released stores to deserialize; they remain baseline evidence until cutover deliberately replaces the obsolete expectations.

Future contract (P) must inspect known location plus legacy serialization signature before creating models. A known serialization header recognizes a retired encoding, not the payload's complete integrity, individual draft count or app version. An unrecognized/malformed header has insufficient evidence for an old-version claim. Keep the original fixtures and backups for recognition/error tests.

| Future read outcome | Required operator/storage result |
| --- | --- |
| No data | Normal empty state |
| Supported valid schema | Open regardless of writer app version |
| Positively recognized retired collection/schema | Notice of unsupported old data; exclude source; separate current store; durable one-time classification/notice state |
| Unsupported newer schema | Requires compatible app; preserve, do not overwrite |
| Corrupt supported payload / unknown format | Damaged/unknown status; preserve diagnostic bytes, no old-version label |
| I/O/permission/disk failure | Storage-access/write error; retain state and permit recovery |

Use format/schema version independently of diagnostic writerAppVersion; new formats must not encode Java package/class names. Atomic JSON envelope is the initial proposal; Room is step 08's evidence-based decision. If cutover isolation/marker writing fails, report I/O failure without deleting old data. Restart must not resurrect uploads or erase a new store. Physical quarantine deletion is a separate explicit cleanup. Downgrade to a pre-cutover APK is not a promised recovery; roll forward with a compatible corrected build.

## Mutation/recovery ordering and external effects

- **Remote save:** SQL submission precedes photo renaming and resumed-draft removal/journal acknowledgement. Local failure after remote success has a distinct visible recovery message. No cross-system transaction combines SQL, files, MediaStore and printing. Never automatically replay an uncertain SQL commit.
- **Capture:** journal identity/snapshot before capture, reserve public row, copy/finalize bytes, record publication, associate with current album/index, retain completed snapshot until acknowledged. Public row plus incomplete journal must not cause duplicate publication on retry.
- **Deletion:** journal before byte mutation; record bytesDeleted before unlinking metadata. Failure may leave public bytes gone but association pending. Retrying metadata must not delete a second time.
- **Import:** selected-folder read permission, public copy/mapping then saved-reference updates, source originals retained; concurrent draft edits are merged. Abandoned source associations are not new draft ownership.
- **Printing:** explicit choice sends Bluetooth commands; failure stops subsequent output. A partially printed receipt is possible. No automatic replay.
- **Reports/viewers:** read SQL data, atomically replace private export, send read-granted view intent. External viewer copies/retention are not controlled by the app.
- **Caches/settings:** refresh/delete and save are local writes distinct from business-record deletion. App backup/uninstall/update behavior needs ACCEPT-07; preservation in code is not a device-upgrade test.
- **FTP:** current service is dormant; helper-only queue progress must not be enabled or replayed during refactoring.

## Evidence anchors and cutover gate

[StoredInspections](../../app/src/main/java/com/juans/inspeccion/data/local/StoredInspections.java), [AtomicObjects](../../app/src/main/java/com/juans/inspeccion/data/local/AtomicObjects.java), [capture](../../app/src/main/java/com/juans/inspeccion/Mundo/Storage/CaptureCoordinator.java), [video capture](../../app/src/main/java/com/juans/inspeccion/Mundo/Storage/VideoCaptureCoordinator.java), [deletion](../../app/src/main/java/com/juans/inspeccion/Interfaz/Gallery/GalleryDeletionModel.java), [photo publication](../../app/src/main/java/com/juans/inspeccion/Mundo/Storage/MediaPhotoStore.java), [video publication](../../app/src/main/java/com/juans/inspeccion/Mundo/Storage/MediaVideoStore.java), [index](../../app/src/main/java/com/juans/inspeccion/platform/media/LocalMediaIndex.java), [import](../../app/src/main/java/com/juans/inspeccion/platform/media/LegacyPhotoImporter.java), [caches](../../app/src/main/java/com/juans/inspeccion/data/local/cache/ColumnasTablas.java), [lists](../../app/src/main/java/com/juans/inspeccion/data/local/cache/Listas.java), [provider roots](../../app/src/main/res/xml/file_paths.xml).

Step 02 adds sanitized recognition/failure fixtures without requiring old deserialization forever. Step 03 must classify every snapshot/sidecar consumer above before persisted-model/package changes. No blanket journal deletion, guessed photo ownership or compatibility shim merely for abandoned drafts is authorized.

## Step 02 preflight and recovery coverage

[StorageFormatProbe](../../app/src/main/java/com/juans/inspeccion/data/local/StorageFormatProbe.java) now tests encoding/envelope recognition independently; it is not called by the app. It distinguishes absent/older/newer/malformed/unknown/I/O results and supported envelope schema independently of writerAppVersion. SUPPORTED_SCHEMA does not validate inspection records; schema 0/1/>1 is the proposed preflight contract, not a shipped storage change. Future adoption must define record semantics/strict encoding/bounds and cutover namespaces/markers/backups before wiring it in (GATE-03-01/02).

Current writers/readers, import controls, queue and journals are unchanged. DraftRecoveryTest extends real serialized save/reopen and backup/error preservation; InspectionSubmissionTest tests real resumed-draft cleanup with fake remote operations. Released fixture hashes were rechecked; all existing deserialization/media/printing tests remain. [Step-02 results and mandatory cutover gates](../testing/step-02-regression.md) prevent treating an unwired classifier or a fake reset as proof of new-format recovery or queue retirement.

## Step 03 cutover: current inventory supersedes the draft/queue rows above

The preceding table describes the pre-cutover step-01/02 baseline and remains historical evidence. [Schema 1 and recovery](draft-schema-v1.md) is now authoritative for draft/notice semantics and bounds. Current namespaces are:

| Current store | Ownership / preservation |
| --- | --- |
| `inspection-drafts-v1.json` + `.bak`/`.new` | Atomic validated UTF-8 JSON; complete active draft graph, IDs and ordering; no Java names |
| `storage-cutover-v1.json` + companions | Validated atomic classification/isolation list and durable acknowledgement |
| `retired-storage/<original-name>.<sha256>` | Byte-preserved recognized drafts/queues/old operations, including their backups/staging; no deserialization or replay |
| `pending-photo-capture-v1`, `pending-video-capture-v1` | New active operations only; retained serializable snapshot/model identities |
| `pending-media-deletion-v1` | New active deletion metadata recovery; retained Result/Kind identities |
| Old unknown/corrupt/unreadable names | Remain in place, reported separately; no cleanup or age guess |
| `photo-publications`, `video-publications` | Preserved original maps and nested class identities; prevent duplicate public publication |
| `legacy-photo-imports` | Preserved string mapping; importing current associated photos does not read retired drafts/queues |
| `local-media-index.json`, public photos/videos, unfinished private capture bytes | Preserved; no ownership guessing, blanket deletion or draft-triggered media deletion |
| SQL/cache/connection/printer/export stores | Same filenames and encoding; outside draft/queue retirement |
| Android extras/saved state | Inspection/damage/album/widget identities and stable component/fragment entry points retained; not a durable draft JSON substitute |

[S/T/E implementation and device limitations](../testing/step-03-cutover-and-moves.md) distinguishes local execution from signed-update/operator acceptance. The serialized-consumer exceptions are deliberate safeguards for active state and independent bookkeeping, not compatibility for abandoned draft files. Step 04 must address widget/form coupling without silently changing these contracts.
