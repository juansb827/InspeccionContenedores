# Readable photo folders — 2026-10-17

New inspection photos use `Pictures/Inspecciones/<direction>_<EP/ET>_<document>_<container>/`. General and damage photos share the same folder. Missing document numbers use `temp`; successful submission updates the associated photos' filenames and folder while keeping their MediaStore URIs. Filename collisions in the destination folder advance the final photo number and preserve existing bytes. Only associated photos move, so unrelated photos sharing a temporary folder remain there.

The user confirmed that the app is unreleased and no UUID-folder compatibility is needed. There is no search of previous UUID folders, migration helper, or fallback to an inspection-ID directory. Arbitrarily named imports go into `Pictures/Inspecciones/`. Internal inspection/album/capture IDs, publication journals, current-operation recovery, private staging names, serialized identities and uploads are unchanged. Existing public photos are not scanned, bulk-moved or deleted.

Focused coverage includes all four inspection prefixes, shared general/damage folders, temporary-name collisions across inspection IDs, target-folder collisions, idempotent submission moves on Robolectric API 29/34, preserving unrelated temporary-folder photos, move failure/retry, current publication recovery, arbitrary imports without UUID folders, and historical pre-29 storage branches. The historical tests do not change minimum SDK 29.

Independent local checks for each committed branch use the configured JBR 21 and SDK:

```sh
./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx3072m -XX:MaxMetaspaceSize=1024m' \
  --max-workers=2 :app:assembleDebug :app:lintDebug :app:testDebugUnitTest \
  --tests '*MediaPhotoStoreTest' --tests '*PhotoNamesTest' --tests '*PhotoRenamingTest' \
  --tests '*CaptureCoordinatorTest' --tests '*LegacyPhotoImporterTest' \
  --tests '*CameraCaptureModelTest'
```

No emulator, physical device, SQL Server, FTP or printer checks are included. Fake-provider tests do not establish OEM filesystem behavior or cross-application races. Previous filename/folder checkpoint reports remain historical evidence.

Modernization verification: the isolated media-only patch on `8ec869e` passed debug assembly, lint and 54 tests in 7 suites with zero failures/errors/skips; all 50 Gradle tasks executed. Lint reported 651 warnings and 7 errors filtered by the existing baseline. Unfinished modernization changes were excluded.

## Video filenames and folders

Videos use the same prefix, numbering and damage convention with `.mp4`, under `Movies/Inspecciones/<direction>_<EP/ET>_<document>_<container>/`. Private staging and pending rows still use internal capture identity; published names contain no UUID. The recording snapshot supplies the name without adding serialized fields. Each new clip requests the first number and the storage adapter chooses the next available one, including pending items. Imports without a recognized prefix use `Movies/Inspecciones/`, never an inspection-ID folder.

Successful submission renames/moves only videos associated through the independent media index, alongside the existing photo-naming path. URI references, media order, playback and photo counters/remote-name maps remain unchanged; videos do not enter the photo upload queue. Rename failure propagates through the existing local naming/recovery outcome instead of deleting bytes or replaying submission. Clearing/removing an association does not delete or bulk-migrate published videos.

Video verification adds `--tests '*MediaVideoStoreTest' --tests '*LocalMediaIndexTest'` to the command above. Coverage includes general/saved/unsaved damage names, journal recreation, pending collisions, publication retry, failed query/move recovery, stable-URI moves on Robolectric API 29/34 and mixed photo/video submission naming. No physical recording, audio, emulator or server checks are included.

Extended modernization verification: the isolated photo/video naming patch passed debug assembly, lint and 67 focused tests in 9 suites, with zero failures/errors/skips. The JVM test task and lint analysis executed; report generation reused unchanged output. No hardware or service checks were added.

Master verification: the adapted cherry-pick independently passed debug assembly, lint and the same 54 focused tests (zero failures/errors/skips), with 50 Gradle tasks executed. Lint reported 651 warnings and 7 baseline-filtered errors.

Extended master verification: the adapted photo/video cherry-pick independently passed debug assembly, lint and all 67 focused tests in 9 suites (zero failures/errors/skips). Test execution and lint analysis ran; report generation reused unchanged output. No unfinished modernization code or hardware/service checks were included.
