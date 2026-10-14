# Current draft schema and recovery

Implemented in step 03 (source and automated execution evidence; operator acceptance remains open). The atomic UTF-8 JSON collection is `inspection-drafts-v1.json`, format `inspection-drafts`, schema version 1. `writerAppVersion`, when supplied by another writer, is diagnostic only. The current writer omits it. No Java class/package names are stored. Room remains outside this change.

The envelope governs all records. Missing/extra required fields, wrong types, duplicate JSON keys, lenient JSON syntax, invalid UTF-8/unpaired surrogate text and invalid relationships are corruption. Another envelope identity/arbitrary data is unknown. Nonnegative integer schema 0 is explicitly retired; versions greater than 1 are newer unsupported data. A recognized legacy serialization signature in an audited old namespace identifies an encoding generation, **not** an originating app version, record count or intact payload.

| Record field | Value / meaning |
| --- | --- |
| `id` | Inspection UUID, unique in the collection |
| `inspectionType`, `documentType` | `ENTRADA`/`SALIDA`, `EP`/`ET`, respectively; null permits an unfinished form |
| `information`, `container` | Complete string-key maps, including the existing entry placeholder’s blank key, hidden/business keys, empty and null values |
| `date` | Null or the existing ordered 3/5/7 string date representation; no date conversion or calendar normalization |
| `inBooking`, `inYard`, `bookingBalance`, `usesTurn` | Actual booleans |
| `bookingRows`, `pendingTurns` | Null or ordered arrays of complete maps |
| `parentFolder` | Nullable historical business/naming value, retained independently of media ownership |
| `albums` | Ordered table: unique album UUID, nonnegative integer `captureCount`, ordered unique photo `references`, complete `remoteNames` map |
| `generalAlbum`, `extraAlbums` | Nullable general UUID and nullable ordered extra UUID list; references must resolve |
| `damages` | Ordered records with complete `details` map and an album UUID |
| `damageTotal` | Finite numeric float, restored exactly without recalculation |

An album shared by general/damage/extra references remains the same object after reading. Every table album must be referenced; album IDs cannot be reused across inspections. Photo references must be valid content URIs with an authority and no fragment, or paths explicitly supported by the existing user-selected legacy-photo importer. No association is inferred from a filename or public-media scan. Captured counters may exceed the remaining photo count. Totals are not forced to match the damage sum: step-02 calculation quirks remain characterized.

Resource bounds: 8 MiB per document; 1,000 inspections; 500 damages and 1,000 album/extra references per inspection; 2,000 photo references per album; 1,000 booking/turn rows; 256 entries per map; 16,384 UTF-16 units per string; JSON nesting at most 32. Writers validate before publishing, and bound collections/strings before building their JSON representation. An invalid proposed write cannot replace the last complete store.

## Atomic recovery

A positively recognized newer primary, backup or staging candidate blocks reading/replacement before backup recovery; it is never discarded in favor of an older supported candidate. Otherwise the last committed `.bak`, when present, takes precedence and must pass full validation before `AtomicFile.openRead` restores it. A damaged backup blocks replacement and remains for diagnosis. A valid primary wins over an unfinished `.new`. A staging-only store is an interrupted write requiring review; it is never an empty store and is not promoted without a committed checkpoint. Writes flush and synchronize bytes, commit atomically using Android `AtomicFile`, and check the published bytes before returning success. Injected pre-commit failure restores the previous committed file. An I/O failure after commit can leave an ambiguous acknowledgement: reopen the store before retrying, using stable IDs.

Current drafts with unknown/corrupt/newer content are not reset, cleared or overwritten. Corrected compatible builds can recover them or a validated committed backup. Operator recovery should first copy the complete primary/backup/staging/marker/archive set, then investigate on an isolated test installation; there is no automated destructive cleanup control in this change.

## Cutover and storage status

`storage-cutover-v1.json` is a separately validated atomic marker containing an envelope/version, acknowledged boolean and exact archived filenames. Recognized old `listaPendientes`, `fotosPendientes`, `pending-photo-capture`, `pending-video-capture`, `pending-media-deletion`, and their `.bak`/`.new` candidates move into `files/retired-storage/<original-name>.<sha256>`. Explicitly retired JSON schema 0 in the current draft namespace is isolated the same way. Current version 1 files are preserved; newer versions are not retired.

There is no Java deserialization during classification/isolation. Hash-named destinations preserve bytes and avoid replacing different evidence. A restart after a rename but before marker publication finds the archived recognized bytes and reconstructs pending notification. Repetition does not replace a new store. Acknowledgement becomes durable only after successful marker publication; cancel/dismiss does not acknowledge. A new isolated item resets the retained acknowledgement marker; it does not trigger a dialog. Unknown/failed queue or old-operation isolation is retained as an unresolved status and never replays or blocks a healthy draft store. An unknown old draft blocks interpreting the absence of current drafts as empty. Marker/read/write failures are explicit errors rather than age guesses.

Following the operator-requested UI change, login, pending inspections and inspection entry do not show an automatic storage disclaimer. **“Estado de almacenamiento”** in Configuración shows only isolated-file counts and unresolved status, dismissed with **“Cerrar”**. The marker acknowledgement fields remain readable for storage compatibility but no longer control automatic UI. No lost-record count or exact source app version is claimed. The upload helper/service does not enqueue, deserialize, connect or transfer; remaining service entry points show **“La cola de fotos está retirada; no se envían archivos.”**

Current photo/video/deletion operations use `pending-photo-capture-v1`, `pending-video-capture-v1`, `pending-media-deletion-v1`. Their active snapshot/result classes retain their names until that separate serialized contract can be replaced safely. Pre-cutover operations are never loaded from their old names. Private unfinished bytes, public media, publication maps, import mappings and `local-media-index.json` are retained. Only associations in retired drafts are unavailable; no ownership guessing or journal blanket deletion occurs.

Downgrading to the pre-cutover APK is **not** data rollback. Recover by rolling forward with a build that understands schema 1. See [storage policy](../modernization/storage-version-policy.md), [storage inventory](storage-and-side-effects.md) and [step-03 evidence](../testing/step-03-cutover-and-moves.md).

Active serialized media recovery/publication/import readers and the independent media index also reject staging-only absence rather than initialize empty bookkeeping. Their schemas/class identities remain unchanged; a valid primary/backup can still recover normally. `MediaStagingRecoveryTest` exercises both real readers on API 29/34. These small preservation guards are in the storage review slice, separate from package moves.
