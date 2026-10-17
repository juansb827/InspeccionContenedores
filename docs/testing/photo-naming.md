# Photo filename adjustment — 2026-10-15

Authorized behavior change: MEDIA-002 uses the released readable JPEG convention without UUID filename suffixes. Internal IDs and `Pictures/Inspecciones/<inspection UUID>/` remain. Missing document numbers use `temp`. Publication and submission renaming choose the requested name or the next available photo number in that folder. General and damage sequences remain separate. No bulk migration, upload activation, video naming, draft-schema or serialized journal-field change is included.

Publication reserves an `IS_PENDING` row with capture identity, journals its URI, then chooses the readable name before copying/publishing. A retry resolves its own row through the existing publication journal; an interruption between insertion and journal writing can recover the temporary reservation by capture identity. Collision queries include pending rows. Local naming and remote-name mappings remain separate, and renaming preserves the URI. Historical API-28 storage-permission/file branches are retained and tested, without changing supported APK versions.

Focused JVM coverage: capture processing and visible names; general/damage collisions and independent folders; preserved existing bytes; pending-row collisions; failed-copy retry and row recovery before journal acknowledgement; completed older publication reuse; idempotent submission renaming through the actual activity method; missing document naming; failed media queries; historical public-file collision handling; existing camera/coordinator/import/submission regression suites.

Executed on 2026-10-15 in the existing modernization working tree, with the configured JBR 21 / Android SDK:

```sh
./scripts/check.sh --tests '*MediaPhotoStoreTest' --tests '*PhotoNamesTest' \
  --tests '*PhotoRenamingTest' --tests '*CaptureCoordinatorTest' \
  --tests '*LegacyPhotoImporterTest' --tests '*CameraCaptureModelTest' \
  --tests '*InspectionSubmissionTest'
./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx3072m -XX:MaxMetaspaceSize=1024m' \
  --max-workers=2 :app:assembleDebug :app:lintDebug
```

Results: 60 tests in 8 suites, zero failures/errors/skips; the test task executed (not UP-TO-DATE). Import-boundary checks passed. Debug assembly and lint passed; lint reported 650 warnings and 7 existing errors filtered by the baseline. This is focused verification, not a full JVM-suite run or a zero-finding lint result.

No emulator, physical device, SQL Server, FTP or printer check was performed for this adjustment. OEM MediaStore behavior and cross-application races remain device acceptance, not something the fake provider proves.

Before pushing on 2026-10-16, the naming-only staged patch was applied to an isolated checkout of `b422c7d`, excluding unrelated modernization edits. `:app:assembleDebug` and `:app:testDebugUnitTest` filtered to `MediaPhotoStoreTest`, `PhotoNamesTest` and `PhotoRenamingTest` passed: 19 tests, zero failures/errors/skips; all 40 Gradle tasks executed. This verifies the published change independently of unfinished working-tree changes. No device or service checks were added.

## Readable folder names — 2026-10-17

The subsequently authorized folder change supersedes the UUID-folder location above for new conventionally named photos. General and damage JPEGs now share `Pictures/Inspecciones/<direction>_<EP/ET>_<document>_<container>/`. Missing documents use `temp`. The existing submission-renaming path updates both `DISPLAY_NAME` and `RELATIVE_PATH` through MediaStore on supported Android versions, preserving the URI and bytes. It resolves filename collisions in the destination folder before moving. Identical readable prefixes (including temporary inspections) can share a folder; capture identity and album associations remain separate. Only associated photo rows move, never an entire folder or unrelated photos.

Existing photos are not bulk-migrated. The pre-release decision explicitly removes UUID-folder lookup and migration compatibility: new capture recovery searches only its current readable folder or uses its journaled URI. Imports without the inspection naming convention use the public `Pictures/Inspecciones/` root, never an inspection-ID folder. Private staging names, IDs, serialized fields/UIDs, independent media order and uploads are unchanged. The subsequent video naming change is documented in [media folders](../photo-folders.md). Historical pre-29 branches remain tests, not supported device behavior.

Additional focused coverage checks all four prefix modes, shared general/damage folders, temporary-folder collisions across different inspection IDs, destination collisions and idempotent submission moves on Robolectric API 29/34, recovery within the current named folder, failed move/retry with preserved bytes, and leaving unrelated photos in a shared temporary folder.

Local verification executed with the configured JBR 21 / SDK:

```sh
./scripts/check.sh --tests '*MediaPhotoStoreTest' --tests '*PhotoNamesTest' \
  --tests '*PhotoRenamingTest' --tests '*CaptureCoordinatorTest' \
  --tests '*LegacyPhotoImporterTest' --tests '*CameraCaptureModelTest' \
  --tests '*InspectionSubmissionTest' --tests '*MediaStagingRecoveryTest'
./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx3072m -XX:MaxMetaspaceSize=1024m' \
  --max-workers=2 :app:assembleDebug :app:lintDebug
```

The completed focused invocation and its immediate script summary reported 70 tests in 9 suites, zero failures/errors/skips; the task executed, and import-boundary checks passed. The shared JUnit report directory was subsequently overwritten by another local test selection, so it is not a durable artifact of this invocation. Debug assembly and lint passed; lint reported 653 warnings and 7 baseline-filtered errors. No full JVM suite, emulator or real device/service checks were included.

The 70-test result above describes the initial folder implementation before the requested compatibility removal. The finalized pre-release behavior and independent branch verification are recorded in [readable photo folders](../photo-folders.md). No old UUID-folder search or UUID-folder fallback is retained.
