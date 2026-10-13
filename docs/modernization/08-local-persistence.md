# 08 — Local persistence decision and optional Room migration

Status: proposed, with an optional Room branch. Depends on step 07. Outcome: validate the versioned storage introduced in step 03 and decide whether Room offers enough benefit to replace its file engine.

## First decide whether migration is worthwhile

At the inspected baseline, `StoredInspections` and `AtomicObjects` serialize drafts and photo queues. The accepted [storage version policy](storage-version-policy.md) retires those old stores; step 03 establishes explicit version detection and new draft storage before model renames. This phase must not reintroduce legacy class wrappers or an old-draft/queue importer.

Assess current draft volume, read/write latency, indexed queries, concurrent updates and operational failures. If versioned atomic files remain sufficient, retain them behind repositories, complete their contract/recovery tests, and proceed to step 09. Room is optional; recognizing unsupported older data is mandatory regardless of the engine. Rebuilding the unused upload feature is not required.

If structured local queries or transaction needs justify migration, use Room for local relational metadata and drafts. Keep photo/video bytes in their existing media stores. Room offers SQLite entities/DAOs, query verification and schema migrations, including Java integration; it does not synchronize SQL Server or perform remote uploads automatically. Choose a release compatible with the app's SDK/toolchain rather than copying a current version untested. [Room documentation](https://developer.android.com/training/data-storage/room)

## Work sequence if Room is selected

1. Propose tables for current drafts, damages, album/media references and operation status. Preserve current IDs, ordering and local-video distinctions. Keep published media bytes and independent indexes intact; old draft-only associations do not need reconstruction.
2. Choose one authoritative store for each record type. Do not leave files, Room and sidecars independently writable for the same fact. MediaStore/file publication cannot be included in a SQLite transaction; preserve recovery bookkeeping for external effects.
3. If step 03's format has already shipped, transfer its supported current records transactionally into Room with interruption-safe completion markers and protection against concurrent edits. No importer for retired Java-serialized drafts or upload queues is required. If the cutover and Room ship together, start a fresh Room store directly after legacy-format recognition; avoid building an unused intermediate importer.
4. Preserve supported source data until transfer verification is complete. Unsupported legacy data remains isolated and identified; corruption, unknown formats and I/O failures remain separate errors. No generic destructive database fallback or silent reset on every upgrade.
5. Export/version Room schemas and test supported migration paths, defaults, nulls, deletion relationships, disk-full errors, interrupted writes and database-open failures. Preserve the distinction between a retired older schema, an unsupported newer schema and damaged data.
6. Document roll-forward recovery with a build that understands current data. Compatibility with the pre-cutover APK is not promised, and reinstalling it may expose abandoned files or miss new drafts. The one-time accepted legacy reset does not authorize losing new-format drafts in a later engine change.

## Required fixtures

- Released draft/upload fixtures recognized without deserializing them; supported current drafts with initialized media IDs.
- Same schema from another app version, retired/newer schema versions, malformed headers/payloads, unknown data, missing files and I/O errors.
- Interrupted/repeated cutover, then new draft creation/reopening; no stale queue replay or new-store reset.
- Capture/deletion/publication bookkeeping, mixed-media order, local-only videos and public-media preservation; isolate unresolved old snapshots without automatic replay.
- If Room is adopted, interruption and concurrent edits during supported-current-format transfer; new edits survive the advertised recovery procedure.

## Review slices and exit criteria

Suggested PRs: storage-engine decision and contract audit; if Room is selected, schema/tests, supported-current-data transfer where needed, and controlled cutover. Format recognition and legacy retirement must already exist from step 03.

- [ ] A retain-versioned-files or adopt-Room decision states its evidence and consequences.
- [ ] Unsupported old/newer formats, corruption and I/O failure have distinct tested outcomes.
- [ ] New-format drafts save/reopen reliably, including after package/model renames.
- [ ] Any Room transfer preserves supported current records and is safe to restart.
- [ ] Public media is unchanged; abandoned upload work never replays or blocks inspection saving.
- [ ] Current-data recovery is tested; no old-draft compatibility wrappers/importer are required.

Recovery: use the tested procedure for the current format, normally a corrected build rather than a downgrade to the old APK. Never delete the new database or overwrite unreadable current records to make a rollback appear successful.
