# Local storage version policy

Decision: **accepted scope; implementation pending**. The user accepts that old drafts and the unused photo-upload queue will be unavailable after the format cutover. Backward-compatible reading or importing of those stores is not required. The app must recognize unsupported older data and explain why it cannot open it.

This supersedes earlier requirements to preserve released draft/queue serialization. It does not authorize deleting public photos/videos, changing SQL Server records, losing newly created drafts, or treating every storage error as an old-version problem. Backend API implementation remains out of scope.

## Recognition contract

Separate the storage format version from the application version. An app update alone does not invalidate data: read any supported format even if another app version wrote it. Changing a Java package must not change the new stored format.

New draft data should use an explicit envelope, readable before constructing application model objects. Proposed fields:

```json
{
  "format": "inspection-drafts",
  "schemaVersion": 1,
  "writerAppVersion": "<diagnostic app version>",
  "records": []
}
```

Use explicit stable field names and plain values without Java class names. Atomic JSON documents are the initial proposal; Room remains a later storage-engine decision. Define record-level schema markers if records may evolve independently; otherwise the envelope version governs the complete collection. The app version is diagnostic metadata, not the compatibility check.

The existing `listaPendientes` and `fotosPendientes` files lack this application-level envelope. Identify their known storage location and legacy serialization signature before attempting object deserialization. This recognizes a retired format, not its exact producing app version or the integrity of every payload byte. Keep original fixtures for recognition tests; old model classes are not needed merely to identify the format. If evidence is insufficient, report unknown/damaged data instead of claiming an older app wrote it.

| Read outcome | Required behavior |
| --- | --- |
| No stored data | Normal empty state |
| Supported schema and valid payload | Open normally, regardless of writer app version |
| Recognized legacy store or explicitly retired older schema | Report unsupported older format; exclude it from active drafts/queue and start a separate current store |
| Newer unsupported schema | Explain that the format requires a compatible app; preserve it and avoid overwriting it |
| Supported schema with invalid/truncated payload, or unknown format | Report damaged/unknown data; preserve it for diagnosis; do not label it an old-version record |
| Permission, disk, or other I/O failure | Report a storage access/write problem; retain state and allow recovery |

Do not infer age solely from `ClassNotFoundException`, `InvalidClassException`, filename, or any generic catch block. Existing drafts are serialized as a collection: the app can identify an unsupported collection without being able to count or identify individual drafts inside it. Do not invent a per-record list or lost-record count.

## Cutover and user experience

1. Inventory all old writers/readers, backup files, and journal references before package moves. Keep the old draft and queue namespaces separate from the new store.
2. On encountering positively identified unsupported old data, persist its classification and exclude it from active reads. Prefer a quarantined copy or isolated original plus an atomic cutover marker; no import or compatibility bridge is needed. If isolation/marker writing fails, show a storage failure and retry safely without deleting the source.
3. Show a notice such as: “Hay borradores guardados en un formato anterior que esta versión ya no admite. Puedes crear nuevos borradores.” Persist notification state so it does not become an endless startup dialog; retain an accessible unsupported-data status until cleanup.
4. Mark the old upload queue as unsupported/retired. Do not replay it, create replacement upload work, or block inspection saving because uploads are unavailable. If upload controls remain, show that the feature is unavailable instead of throwing an exception or claiming success. Rebuilding photo upload is not a requirement of this modernization.
5. New drafts must save/reopen normally in the explicit current format. Restarting during cutover must neither resurrect the retired queue nor clear a new store. Physical deletion of quarantined data is a separate explicit cleanup action; it is not needed to complete the cutover.

Public media bytes and the independent media index remain intact. Associations contained only in discarded drafts need not be reconstructed; do not attach their photos/videos to newly created drafts by guessing IDs. Inspect capture/deletion/publication journals separately: some embed old model snapshots. Preserve their files and public-media effects, isolate unresolved legacy operations visibly, and prevent stale replay. Preserve or migrate independent publication bookkeeping as needed to avoid duplicate publication or deletion. Draft/queue retirement is not permission for blanket journal deletion.

## Sequencing, tests, and rollback

Step 01 records this behavior; step 02 adds recognition/error fixtures. Step 03 implements a small storage cutover PR **before** renaming persisted models. Step 08 evaluates Room for the already established current format; it does not reintroduce an old-draft importer.

Test known legacy drafts/queues, supported records written by a different app version, retired/newer schema versions, malformed headers/payloads, missing files, I/O failure, interrupted/repeated cutover, and creation/reopening of new drafts. Ensure public media is unchanged, uploads do not replay, and errors are accurately classified. Replace old “must deserialize” expectations at cutover while retaining media and current-format recovery tests.

Downgrade compatibility with the pre-cutover app is not promised. Treat the cutover as a roll-forward release: recover via a corrected build understanding the current schema. An old APK may reopen abandoned files or miss new drafts, so simply reinstalling it is not the recovery procedure. The accepted one-time break does not grant permission to reset drafts on every future release.
