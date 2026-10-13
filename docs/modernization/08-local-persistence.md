# 08 — Local persistence decision and optional Room migration

Status: proposed, with an optional implementation branch. Depends on step 07. Outcome: deliberate ownership of durable local data, with either a justified retention decision or a tested migration.

## First decide whether migration is worthwhile

Today `StoredInspections` and `AtomicObjects` preserve serialized drafts and photo queues, while media recovery/index data uses separate files. Recent tests cover corrupt files, atomic writes, legacy import and capture recovery. Do not discard working recovery logic for a fashionable persistence library.

Assess draft/queue volume, read/write latency, need for indexed queries, concurrent updates, schema evolution, and operational failures. If atomic files remain sufficient, record a decision to retain them behind repositories, complete their contract/recovery tests, and proceed to step 09. This is the recommended default until measurements justify change.

If structured local queries or transaction needs justify migration, use Room for local relational metadata and drafts. Keep photo/video bytes in their existing media stores. Room offers SQLite entities/DAOs, query verification and schema migrations, including Java integration; it does not synchronize SQL Server or perform remote uploads automatically. Choose a release compatible with the app's SDK/toolchain rather than copying a current version untested. [Room documentation](https://developer.android.com/training/data-storage/room)

## Work sequence if Room is selected

1. Write a schema proposal for drafts, damage rows, album/media references, upload items and operation status. Preserve existing stable IDs, ordering, remote names, local-video distinction, and unknown legacy business fields. Avoid introducing sync/conflict semantics without a feature requirement.
2. Choose one authoritative store for each record type at each migration stage. Do not leave Room, serialized files and media sidecars independently writable for the same fact. MediaStore/file publication cannot be included in a SQLite transaction; preserve a journaled reconciliation boundary for external effects.
3. Import using the original compatible serialized classes. Read, validate and map a snapshot; write it in a local transaction with an idempotent migration marker. Coordinate with current draft/capture/upload activity so a concurrent edit or acknowledged transfer cannot be overwritten or resurrected.
4. Preserve the source files until verification and the rollback window are complete. Mark unreadable files as recoverable failures; never replace them with empty data or use destructive database recreation for business records.
5. Export and version Room schemas; provide explicit migration tests for every supported version path. Test defaults, nulls, deletion relationships, interrupted writes, disk-full errors and database-open failures.
6. Define downgrade behavior before rollout. An old APK reading only a stale serialized snapshot would lose all post-migration edits. Either provide a tested compatible export for rollback, keep migration disabled during a defined compatibility window, or declare a roll-forward-only release with a tested recovery procedure. A retained backup alone is not rollback compatibility.

## Required fixtures

- Released `d9c52d3` draft/upload fixtures, plus recent drafts with initialized media IDs.
- Empty stores, multiple drafts, unsaved damage albums, missing media, damaged files, and revoked legacy-folder access.
- Pending capture/deletion/publication, partially acknowledged photo uploads, mixed photo/video ordering, and local-only videos.
- Process death at every migration boundary; repeat import; edit or acknowledgement arriving during import.
- Upgrade, then new edits, then the proposed rollback/recovery procedure.

## Review slices and exit criteria

Suggested PRs if selected: schema/decision; read-only importer tests; transactional import and recovery; controlled cutover; retirement of obsolete writers after the compatibility window.

- [ ] A retain-files or adopt-Room decision states its evidence and consequences.
- [ ] If retained, repository contracts and recovery tests cover the active file formats.
- [ ] If migrated, all fixture data, IDs, media order and pending operations survive; repeat/restarted migration is safe.
- [ ] No destructive fallback is enabled, and public media is not copied/deleted unnecessarily.
- [ ] Post-upgrade edits survive the advertised rollback/recovery strategy.
- [ ] Original package compatibility classes remain until all supported import/rollback paths no longer require them.

Rollback: follow the tested storage-specific procedure. Never assume reverting a Room dependency or deleting the new database safely restores user work.
