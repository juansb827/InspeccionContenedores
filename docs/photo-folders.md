# Readable photo folders — 2026-10-17

New inspection photos use `Pictures/Inspecciones/<direction>_<EP/ET>_<document>_<container>/`. General and damage photos share the same folder. Missing document numbers use `temp`; successful submission updates the associated photos' filenames and folder while keeping their MediaStore URIs. Filename collisions in the destination folder advance the final photo number and preserve existing bytes. Only associated photos move, so unrelated photos sharing a temporary folder remain there.

The user confirmed that the app is unreleased and no UUID-folder compatibility is needed. There is no search of previous UUID folders, migration helper, or fallback to an inspection-ID directory. Arbitrarily named imports go into `Pictures/Inspecciones/`. Internal inspection/album/capture IDs, publication journals, current-operation recovery, private staging names, serialized identities, video storage and uploads are unchanged. Existing public photos are not scanned, bulk-moved or deleted.

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
