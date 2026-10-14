# Step 03: cutover and package execution evidence

Evidence date: 2026-10-13 (local timezone; XML timestamps may be UTC 2026-10-14). Starting HEAD `0a365c1e8035f38fe13c358704fb6ab8e2a83e10`, plus the preserved step-01/02 working tree. No commit or production-service execution. The configured JDK/SDK are documented in [baseline](baseline.md); no machine paths or credentials were added to executable tracked configuration.

## Storage prerequisite (GATE-03-01/02)

The real [schema/reader/writer/cutover](../architecture/draft-schema-v1.md) replaced the unwired preflight contract. `DraftCutoverTest` has 28 cases executed on Robolectric API 29 and 34; `StorageNoticeTest` exercises actual dialog close/acknowledgement and persistent status. Total addition: 29 test methods / 57 JVM executions. Legacy fixture bytes are unchanged. Only deliberately superseded draft/queue deserialization/replay expectations were replaced; media/import/printing and step-02 business/request characterization remain.

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

## Package milestone (GATE-03-03)

[84-class inventory/exceptions](../architecture/package-moves.md): 31 UI, 18 media, 35 data/platform production moves, 22 main-source entry/fragment bridges and stable configured runner/debug-host bridges. `p1`/`p2` now have descriptive implementation names. Active model/snapshot/publication/deletion/cache/widget identities remain deliberate exceptions; no persisted-model rename is claimed.

| Fresh/focused execution | Actual result |
| --- | --- |
| UI group: FormBindingTest / PrinterPermissionTest | 14 executions, 2 suites, pass; production and all JVM sources compiled |
| Media group: camera/gallery/index/capture/cutover | 102 executions, 9 suites, pass |
| Data/platform group: business/request/submission/storage/printing/component checks | 171 executions, 17 suites, pass |
| Staging guards / component / cutover focused check | 62 executions, 3 suites, pass |
| Final `./scripts/check.sh --fresh` | **239 executions / 30 suites**, zero failures/errors/skips; **74 tasks executed**, 2m57s; fresh debug/test APK and lint |
| Final unsigned `:app:assembleRelease --rerun-tasks` | **44 tasks executed**, 1m36s, pass; signing remains null for release |
| Final debug lint | **645 warnings, seven existing baseline-filtered errors**; baseline issue attributes/messages/source lines unchanged, only six file paths relocated |

The 65-execution increase from the fresh 174 baseline consists of 35 new test methods: 28 cutover cases × API 29/34, one actual notice case, four package identity/XML/UID cases and two staging preservation cases × API 29/34. Existing 174 cases remain except their explicitly superseded storage/queue expectations. Java/source-target warnings and Robolectric image-decoder diagnostic output remain; neither was hidden or used to skip tests.

Development failures were resolved before final checks: deliberate old deserialization/replay expectations were replaced at cutover; import/test-helper package references and the instrumentation superclass were corrected during moves. The initial media-fixture failure was caused by an unsupported `/old/...` reference. Its failed deletion metadata checkpoint also made an immediate retry nonempty/busy; only the dedicated disposable fixture installation was cleared before the successful pre-move gallery run. No application recovery journal reset behavior was added. Final package installation used `install -r` on that same dedicated installation, and the storage runner reopened pre-move current drafts/publication maps/capture descriptors before resetting its own fixture collection.

The late audit guards reject staging-only serialized bookkeeping/media-index reads. Legacy recognition now reads four header bytes before applying current JSON size limits, so large old collections can be recognized without reading/deserializing their payload; a fault after the header is not payload-integrity evidence. Isolation still hashes/reads actual source bytes and reports any failure as I/O. These changes are assigned to the independent storage review slice. Reversing package/class names and removing imports produces identical bodies for **all 84 moved production classes**; SQL-sensitive string-literal comparison also found no differences.

## Dedicated local device execution (E; not O)

A separate API 34 AVD used serial `emulator-5580`, headless software rendering, 1080×1920 pixels / density 420, default font scale. The existing API 37 emulator was not modified. Permissions were granted only on the dedicated fixture installation.

- Storage suite: pass on real Android files/AtomicFile, acknowledgement, current draft reopen/failed commit preservation, no queue replay, old/new registered entries, old information/damage fragment class names and all five operator information/damage layouts plus inspection/login XML. FragmentFactory resolution is not authenticated screen attachment.
- Gallery: **33 checks passed**, including deletion, restricted modes, selection/back and Activity/fragment recreation.
- Embedded camera: **14 checks passed**, including actual CameraX capture and local lifecycle/association paths.
- Video: the final runner reported **37 checks passed**; the enclosing multi-suite shell ended with status 143. The standalone repeat also reported **37 passed** and the adb command exited **0**, resolving the shell/transport status concern.
- Public retention: **167 JPEGs** had identical SHA-256 listings before/after pre-move cutover; **176 JPEGs and one MP4 (177 files)** remained byte-identical across package installation and repeated cutover. No public-media directory was cleared. Ordinary suite-confirmed fixture deletion is distinct from cutover retention checks.
- The gallery runner captured grid/viewer PNGs, pulled and visually inspected at 1080×1920. The grid capture includes asynchronous loading placeholders; the viewer shows the loaded first fixture. These are local fixture screenshots, not an operator/fleet walkthrough or layout redesign acceptance.

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

Session snapshots/delta patches (`storage-change.patch`, `package-moves.patch`, produced in the session artifact directory) separate storage from package-only code and preserve the pre-session working tree. The storage review base includes the preservation/header-recognition follow-ups in their original packages; the move slice also includes new package verification tests. Documentation is the final handoff slice. Android identities and independent serialized consumers are explicitly audited; old models are retained solely for active journal/Bundle/cache contracts. [Step 04](../modernization/04-data-and-business-boundaries.md) must extract plain form inputs/typed inspector lookup before broad repositories, remove widget serialization safely, preserve schema 1 and characterized quirks, and replace serialized journal contracts before moving their models. Pre-cutover APK downgrade is not data rollback.

Final consistency checks resolved every local Markdown target; external URLs were retained without revalidation. Legacy binary fixture SHA-256 values, original IDE-file bytes and the unrelated `22# Photo gallery` title were preserved. No dependencies/build configuration, SQL literals, resource IDs/layout structure, package-independent storage schema, application identity or signing settings were changed by the move slice. No test was disabled and the lint baseline was not broadened.

A final preservation case verifies that a newer primary cannot be replaced by a supported backup, and newer staging cannot be discarded by a new save. Real classification precedes atomic backup selection; all candidate bytes remain intact. The dedicated storage runner also exercises this newer-primary/backup refusal. The final 239-execution / 2m57s full check and 1m36s unsigned release supersede the earlier 237-execution milestone. The final storage device command passed and adb exited 0.
