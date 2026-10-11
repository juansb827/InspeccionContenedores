# Embedded camera

The CameraX photo checkpoint replaces external-camera launch with an inspection-hosted,
full-window camera screen. All capture buttons use `openCamera(Album, damageIndex)`;
opening or canceling the screen creates no file. The private destination and unchanged
photo recovery journal are created only when the shutter is pressed.

CameraX core/camera2/lifecycle/view are pinned to 1.4.2. Java/XML, compile SDK 34,
target SDK 33, minimum SDK 29, application ID and signing configuration are retained.
The screen uses the rear camera with flash off, tap-to-focus and pinch zoom. Camera
permission is requested at entry; denial preserves the inspection and permanent denial
provides a settings action. Capture restrictions apply in handlers as well as buttons.

Publication runs in an activity-scoped ViewModel and workers never mutate a view's
Album. Completion resolves the current inspection/editing album by ID. Existing JPEG
processing, remote naming, public publication and serialized draft formats are retained.
Interrupted captures can retry saving existing bytes without launching a new capture.

Validation on 2026-10-11: debug and unsigned release assembly, all 77 JVM tests and
lintDebug passed (no new lint errors; existing baseline warnings remain). The local
instrumentation camera suite passed 14 checks on API 29 and API 34, capturing actual
emulator camera output in all four type/direction combinations, plus editing-album
recreation and cancel-before-capture. The debug host is absent from release APKs.

```sh
JAVA_HOME=/home/juanb/.jdks/jbr-21.0.11 ./gradlew assembleDebug assembleDebugAndroidTest testDebugUnitTest lintDebug assembleRelease
adb shell pm grant com.juans.inspeccion android.permission.CAMERA
adb shell am instrument -w -e suite camera com.juans.inspeccion.test/com.juans.inspeccion.Interfaz.Gallery.GalleryInstrumentation
```

APKs, commit IDs and check logs are saved outside tracked files under
`~/.local/state/inspeccion-validation/2026-10-11/camera-video/`.
These local tests use no production credentials. Physical inspection-device camera,
authenticated inspection screens, printer and server workflows remain separate manual
acceptance checks and were not tested here.
