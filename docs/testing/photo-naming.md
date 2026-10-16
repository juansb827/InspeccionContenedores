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
