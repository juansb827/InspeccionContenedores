# Step 01 testing environment and baseline results

Evidence: E/S/T, 2026-10-13. Source baseline: `0a365c1e8035f38fe13c358704fb6ab8e2a83e10`, version 10 / 1.10, working tree inspected rather than historical checkout. Initial unrelated edit: `.idea/misc.xml`; left untouched. The four modernization inputs were read from the working tree before source inspection. No tests, application files, dependencies or build configuration were changed.

## Environment and repeatable commands

This Linux session used JBR 21.0.11 and the locally configured Android SDK with compile platform 34 and accepted licenses. Default `java`, `adb` and `emulator` were not on shell PATH; explicit installed tools worked. The tracked wrapper is Gradle 8.11.1; AGP is 8.10.1. Configure JAVA_HOME to a compatible installed JDK (JBR 21 is the verified choice here), Android SDK through untracked `local.properties` (`sdk.dir`) or local environment, and add SDK platform-tools to PATH for adb. Do not hardcode a developer home directory into CI or tracked build files. Dependency resolution may require Google/Maven Central/restricted JitPack and Robolectric SDK artifacts; it does not require business-service credentials.

After setting JAVA_HOME locally, these are the exact task/heap arguments used, from repository root:

```sh
./gradlew --no-daemon \
  -Dorg.gradle.jvmargs='-Xmx3072m -XX:MaxMetaspaceSize=1024m' --max-workers=2 \
  assembleDebug assembleDebugAndroidTest testDebugUnitTest lintDebug assembleRelease

# Force fresh test execution after the first invocation reused its test output:
./gradlew --no-daemon \
  -Dorg.gradle.jvmargs='-Xmx3072m -XX:MaxMetaspaceSize=1024m' --max-workers=2 \
  :app:testDebugUnitTest --rerun-tasks
```

No permanent heap/build setting was added. The second command reruns its dependencies too; it was used because `testDebugUnitTest` was UP-TO-DATE in the first invocation, not to hide a failure. The first build reused many unchanged tasks; this was not a clean build from an empty cache.

| Check | Result actually obtained |
| --- | --- |
| Combined build/test/lint/release invocation | Exit 0; `BUILD SUCCESSFUL in 1m 41s`; 117 actionable tasks: 22 executed, 95 up-to-date |
| Debug assembly | Successful; assembly/package work executed |
| Debug instrumentation APK assembly | Successful, assemble task up-to-date; **device tests not executed** |
| First JVM test task | UP-TO-DATE; not fresh execution evidence |
| Forced JVM invocation | Exit 0; `BUILD SUCCESSFUL in 1m 22s`; 24 tasks executed |
| Fresh JUnit XML | 18 suites, 94 tests, 0 failures, 0 errors, 0 skipped |
| lintDebug | Successful with existing baseline: **645 warnings, 7 errors filtered by `lint-baseline.xml`**; not a clean lint report |
| Release assembly | Successful unsigned APK; no production signing/deployment/upgrade verification |
| adb environment read | One connected emulator, API 37 (`Medium_Phone_API_37.0`), app already installed. Read-only device listing/version/package checks only. |

Build warnings include deprecated `android.defaults.buildfeatures.buildconfig`, obsolete Java 8 source/target on JBR 21 and deprecated API use. Fresh JVM output included “Failed to create image decoder with message 'unimplemented'”; XML still reported zero failed tests. No build warnings were fixed. No coverage percentage was measured.

Generated evidence locations (relative, regenerated/ignored build outputs): `app/build/test-results/testDebugUnitTest/TEST-*.xml`, `app/build/reports/tests/testDebugUnitTest/index.html`, `app/build/reports/lint-results-debug.html`, `build/reports/problems/problems-report.html`, `app/build/outputs/apk/debug/app-debug.apk`, `app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk`, `app/build/outputs/apk/release/app-release-unsigned.apk`. Session stdout was retained temporarily outside tracked files; the durable result summary is this document. No original checkpoint logs were treated as current evidence.

## Existing JVM inventory (fresh XML agrees with source counts)

| Suite | Tests | Main boundary |
| --- | ---: | --- |
| StorageCompatibilityTest | 6 | Released collection reads, damaged/replacement preservation, identities |
| CaptureCoordinatorTest | 9 | Photo identity, cancel, retry, publication and snapshot recovery |
| VideoCaptureCoordinatorTest | 6 | Video finalization/limits, discard/recovery/association |
| MediaPhotoStoreTest | 6 | Public publication, pending rows, failure, historical permission branches |
| MediaVideoStoreTest | 3 | Public video publication and validation |
| LocalMediaIndexTest | 4 | Mixed ordering, video independence, atomic error preservation |
| ImageProcessorTest | 5 | JPEG size/rotation/mirror/processing failures |
| PhotoNamesTest | 2 | Local identity versus remote document naming |
| LegacyPhotoImporterTest | 6 | Original preservation, idempotency/cancel/fail/concurrent draft merge |
| UploadQueueTest | 3 | Fake FTP progress/retry/resource closure; no active upload acceptance |
| FileConsumersTest | 5 | FileProvider limits, read grants, private PDF/XLS and replacement failure |
| GalleryAlbumsTest | 5 | Album identity, mode admission and labels |
| GalleryDeletionModelTest | 6 | Reference-based deletion, failure/recreated owners, metadata retry |
| PhotoGalleryFragmentTest | 5 | Grid/viewer/back/recreation/empty/unavailable/restricted controls |
| CameraCaptureModelTest | 9 | Capture/video retained state, admission, publication and limits |
| CameraCaptureFragmentTest | 4 | Camera screen opening/cancel/permission-denied behavior |
| PrinterPermissionTest | 6 | Permission grant/denial, enablement, historical SDKs |
| PrinterTransportTest | 4 | No commands after denial/failed connection/revocation/command failure |
| **Total** | **94** | Mostly storage/media/printer helpers and local UI; not business integration |

Tests are under [JVM sources](../../app/src/test/java/com/juans/inspeccion). `Mundo/Recibos/RecibosTest` in main sources is a production helper, not an automated test suite. No damage business-rule, form-binding, real login/inspection submission or SQL dialect/transaction tests were found. No tracked CI workflow was found under `.github`.

## Focused selection for later work

Run affected existing suites, then appropriate assembly/lint checks; do not substitute a narrow pass for missing acceptance. Examples below are command guidance, **not extra commands executed in step 01**:

```sh
./gradlew :app:testDebugUnitTest --tests '*StorageCompatibilityTest'
./gradlew :app:testDebugUnitTest --tests '*CaptureCoordinatorTest' --tests '*LocalMediaIndexTest'
./gradlew :app:testDebugUnitTest --tests '*Gallery*Test' --tests '*Camera*Test'
./gradlew :app:testDebugUnitTest --tests '*Printer*Test'
./gradlew :app:assembleDebug :app:lintDebug
```

The [historical Robolectric resource override](../../app/legacy-robolectric.gradle) lowers only test-copy minSdk to 22 so API 22/28 regression fixtures still run. APK support remains API 29+. Do not advertise API 22/28 device support based on those tests.

## Device commands and limits (not run here)

The custom [GalleryInstrumentation](../../app/src/androidTest/java/com/juans/inspeccion/Interfaz/Gallery/GalleryInstrumentation.java) runner delegates `suite camera` / `suite video` to [CameraInstrumentation](../../app/src/androidTest/java/com/juans/inspeccion/Interfaz/Camera/CameraInstrumentation.java). It uses a debug-only local fixture host, not authenticated production screens.

Use a dedicated disposable test emulator/device, a selected serial and the current built APKs. These suites create/delete local public fixture media and may replace fixture drafts; do not run against an operator's app data. Building the test APK does not execute them. We did not install, clear app data, grant permissions, run suites or capture screenshots on the already-running API 37 emulator because its test-data suitability and matrix compatibility were not established.

```sh
# Set DEVICE_SERIAL to a dedicated test device; adb must be on PATH.
adb -s "$DEVICE_SERIAL" install -r app/build/outputs/apk/debug/app-debug.apk
adb -s "$DEVICE_SERIAL" install -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s "$DEVICE_SERIAL" shell am instrument -w \
  com.juans.inspeccion.test/com.juans.inspeccion.Interfaz.Gallery.GalleryInstrumentation
adb -s "$DEVICE_SERIAL" shell pm grant com.juans.inspeccion android.permission.CAMERA
adb -s "$DEVICE_SERIAL" shell am instrument -w -e suite camera \
  com.juans.inspeccion.test/com.juans.inspeccion.Interfaz.Gallery.GalleryInstrumentation
adb -s "$DEVICE_SERIAL" shell pm grant com.juans.inspeccion android.permission.RECORD_AUDIO
adb -s "$DEVICE_SERIAL" shell am instrument -w -e suite video \
  com.juans.inspeccion.test/com.juans.inspeccion.Interfaz.Gallery.GalleryInstrumentation
```

Preserve the custom runner rather than replacing it implicitly. Historical docs report API 29/34 gallery/camera/video results; those are H, not rerun here. No production SQL/FTP, operator walkthrough, physical printer/camera/microphone, device upgrade/downgrade or authenticated business workflow was exercised. Do not use `connectedDebugAndroidTest` as proof these custom suite choices were executed unless its behavior/results are established.

## Fixtures and format provenance

[released-d9c52d3 README](../../app/src/test/resources/released-d9c52d3/README.md) states that fixtures were generated from released model sources with `javac --release 8`, without launching the app. This establishes fixture construction, not an operator-device archive or the app version that wrote arbitrary old user files. Keep bytes for future recognition rather than requiring old deserialization forever.

| Fixture | First four bytes verified | SHA-256 verified in step 01 |
| --- | --- | --- |
| `listaPendientes` | `ac ed 00 05` | `d51e67d823ddc83923de482ffce0d1959564fef83461aab95365d81f3d4c7b49` |
| `fotosPendientes` | `ac ed 00 05` | `80718b10b7e13b24c6e7b9bb55d91750209c205596fb1c581d1108d7e775a5eb` |

Fakes/local resources use synthetic media and transfer clients. New step-02 fixtures must be sanitized and deterministic, with explicit expected classification. SQL integration later needs a dedicated SQL Server schema/version, disposable records and injected connection settings. Never load the operator's settings, use hardcoded production probes or publish query/connection logs containing data/credentials.

## Documentation verification (E)

A local Python/pathlib scan resolved Markdown local link targets across root README/AGENTS and all docs: **209 targets, zero missing targets**; there were no local fragment links to validate. External URLs were retained but not fetched or revalidated. A separate scan found **10 feature documents and 59 own-prefix stable behavior IDs**, all represented in the acceptance matrix. Newly added Markdown had no trailing whitespace. `git diff --check` passed. Scope review found only documentation edits plus the unrelated pre-existing `.idea/misc.xml` diff; a concurrent `22` prefix on the photo-gallery title was preserved. Historical-body comparison against HEAD passed for all three checkpoint files; the title differs because of that retained edit. No source, resource, tests, fixtures, SQL, dependencies or build configuration changes. No commit was created for this baseline.

## Step 02 update

The 94-test results above remain step-01 evidence. Step 02 independently reproduced them before adding tests, then executed **174 JVM tests in 26 suites**, with zero failures/errors/skips, plus fresh debug/test APK assembly and lint. Lint remained at 645 warnings and 7 baseline-filtered errors. [Step-02 implementation/results](step-02-regression.md) gives exact commands, new coverage, narrow production seams and open gates. The new portable entry point is `./scripts/check.sh --fresh`; use repeated `--tests` options for a focused JVM run. The GitHub Actions workflow is configured but has not been executed; no hosted/device/service acceptance is claimed.
