# Readable photo filenames — 2026-10-16

This is the media-only backport of `8ec869ed7e9c3ebea5e1e21d5238254d8c5a3281` to master, adapted to its existing Java packages. It does not bring in the modernization branch's package moves, draft cutover, repositories or database experiments.

Public JPEG filenames follow the existing document/container convention without UUID suffixes:

- General: `<direction>_<EP/ET>_<document>_<container>_<photo>.jpg`.
- Damage: `<direction>_<EP/ET>_<document>_<container>_DANIO#<damage>_<photo>.jpg`.
- Before a document number exists, use `temp`; the existing successful-submission path renames the associated photos afterward.
- When a requested filename is occupied in the same folder, advance the final photo number. Preserve the existing image; the selected name remains available through its stable MediaStore URI.

The public folder stays `Pictures/Inspecciones/<inspection UUID>/`. Internal capture/album IDs, serialized fields/identities, publication journals, remote-name mappings and video behavior remain. Existing photos are not bulk-renamed.

Publication first reserves a pending row using capture identity, records its URI in the existing journal, then chooses the readable filename before copying and publishing. Recovery can reuse the same row, including an interruption between insertion and journal writing. Collision queries include pending rows. Submission renaming uses the same collision protection and keeps the URI stable.

## Verification

The isolated master checkout uses the configured JBR 21 and Android SDK. The verification command is:

```sh
./gradlew --no-daemon -Dorg.gradle.jvmargs='-Xmx3072m -XX:MaxMetaspaceSize=1024m' \
  --max-workers=2 :app:assembleDebug :app:lintDebug :app:testDebugUnitTest \
  --tests '*MediaPhotoStoreTest' --tests '*PhotoNamesTest' --tests '*PhotoRenamingTest'
```

No emulator, physical device, SQL Server, FTP or printer check is included. Fake-provider tests do not prove OEM MediaStore behavior or cross-application races.

Executed results: debug assembly, lint and all 19 focused tests passed (zero failures/errors/skips); all 50 Gradle tasks executed. Lint reported 650 warnings and 7 errors filtered by the existing baseline. These checks ran against the isolated master port, without unfinished modernization edits.
