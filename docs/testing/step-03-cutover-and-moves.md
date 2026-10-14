# Step 03: cutover and package execution evidence

Evidence date: 2026-10-13 (local timezone; XML timestamps may be UTC 2026-10-14). Starting HEAD `0a365c1e8035f38fe13c358704fb6ab8e2a83e10`, plus the preserved step-01/02 working tree. No commit or production-service execution. The configured JDK/SDK are documented in [baseline](baseline.md); no machine paths or credentials were added to executable tracked configuration.

## Storage prerequisite (GATE-03-01/02)

The real [schema/reader/writer/cutover](../architecture/draft-schema-v1.md) replaced the unwired preflight contract. `DraftCutoverTest` has 27 cases executed on Robolectric API 29 and 34; `StorageNoticeTest` exercises actual dialog close/acknowledgement and persistent status. Total addition: 28 test methods / 55 JVM executions. Legacy fixture bytes are unchanged. Only deliberately superseded draft/queue deserialization/replay expectations were replaced; media/import/printing and step-02 business/request characterization remain.

| IDs / contract | Actual assertions |
| --- | --- |
| DRAFT-001/002/004/006 | Complete fields, hidden keys, null/empty values, booleans, dates, row/damage/album ordering, shared album identity, stable IDs and unrecalculated total; save/invalidate/reopen through Pendientes/StoredInspections |
| DRAFT-004/006 | Real strict JSON/UTF-8/record/reference/type/version validation; resource limits; corrupt/unknown/newer/I/O refusal; actual bytes preserved on failed replacement |
| DRAFT-006 / UPLOAD-004 | Legacy primary/backup/staging isolation; explicit retired JSON version; supported complete records from another writer version; interrupted rename before marker and failed marker commit; repeated cutover/acknowledgement and new drafts survive |
| MEDIA-005/UPLOAD-001/004 | Actual capture snapshot isolated without publishing/discarding private bytes; unknown old operations use separate current names; independent maps/index preserved; queue read/enqueue/transfer retired; queue isolation failure does not block healthy drafts |
| DRAFT-006 notice | Real dialog “Cerrar” does not acknowledge, “Entendido” persists acknowledgement; later startup avoids the notice, status can still be opened explicitly |

Fresh baseline: `JAVA_HOME=<configured JDK> ./scripts/check.sh --fresh` passed **174 tests / 26 suites**, 74 executed tasks, 2m44s. This was fresh execution, not the previous step's XML.

Completed storage freeze: the same fresh command passed **229 JVM executions / 28 suites**, zero failures/errors/skips; debug and Android-test APK assembly and lint ran freshly. Lint retained **645 warnings / seven baseline-filtered errors**. `:app:assembleRelease --rerun-tasks`, with the documented heap/workers/JDK, passed 44 executed tasks and produced an unsigned release APK. Release vital lint is a separate narrower check, not a replacement for debug lint.

The dedicated API 34 local storage runner passed real filesystem cutover/acknowledgement, atomic draft failure preservation/reopen, no queue replay, registered old components and launcher/XML inflation. The first gallery run exposed a debug fixture path outside the supported schema, followed by failed metadata save. Its unavailable-media reference was replaced with a valid missing content URI; the bounded counter wait retains the same assertion. That test-fixture change is part of the storage review slice, not an application gallery behavior fix.

## Package milestone

Pending until the storage freeze and unsigned release gates have completed. [Move inventory and exceptions](../architecture/package-moves.md) defines the reviewed small groups. Final focused/full/release/device results are recorded here when actually completed; no device build is treated as a run.

## Reproduction and evidence limits

```sh
JAVA_HOME=<configured JDK> ./scripts/check.sh --fresh
JAVA_HOME=<configured JDK> ./scripts/check.sh --tests '*DraftCutoverTest' --tests '*StorageNoticeTest'
JAVA_HOME=<configured JDK> ./gradlew --no-daemon \
  -Dorg.gradle.jvmargs='-Xmx3072m -XX:MaxMetaspaceSize=1024m' --max-workers=2 \
  :app:assembleRelease --rerun-tasks
adb -s "$DEVICE_SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$DEVICE_SERIAL" install -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s "$DEVICE_SERIAL" shell am instrument -w -e suite storage \
  com.juans.inspeccion.test/com.juans.inspeccion.Interfaz.Gallery.GalleryInstrumentation
# Existing gallery default; camera/video use -e suite camera / video, with local permissions granted.
```

The custom storage suite deliberately replaces its local fixture draft/marker/archive state. Run all suites only on a dedicated disposable device, never an operator installation. This session created a separate API 34 AVD/serial; the already-running API 37 device was untouched. Local emulator evidence does not establish physical optics/microphone/printer output, long-video/200 MB boundaries, full authenticated business-screen lifecycle, signed deployed-APK update or operator acceptance. No SQL/FTP service, credentials or customer records were used. SQL dialect/atomicity/concurrency remain step 05 gates.

## Review separation and handoff

Session snapshots/delta patches separate the storage cutover from subsequent package-only moves and preserve the pre-session working tree. Android identities and independent serialized consumers are explicitly audited; old models are retained solely for active journal/Bundle/cache contracts. [Step 04](../modernization/04-data-and-business-boundaries.md) must extract plain form inputs/typed inspector lookup before broad repositories, remove widget serialization safely, preserve schema 1 and characterized quirks, and replace serialized journal contracts before moving their models. Pre-cutover APK downgrade is not data rollback.
