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

## Local video checkpoint

CameraX `camera-video:1.4.2` and Media3 ExoPlayer/UI `1.4.1` are pinned. Their AAR
metadata and assembly retain compile SDK 34 (minimum 29, target 33). References:
[CameraX releases](https://developer.android.com/jetpack/androidx/releases/camera#1.4.2),
[Media3 releases](https://developer.android.com/jetpack/androidx/releases/media3#1.4.1).

Each fresh camera opening starts in Foto. Video requests microphone permission and
requires audio; denial leaves Foto available. The rear camera records SDR MP4, HD
(720p) when supported and SD (480p) otherwise. Unsupported devices retain photo
capture. File output options enforce 300,000 ms and 200,000,000 bytes. Back during
recording offers Guardar / Descartar / Continuar. Leaving the foreground or recreating
the screen stops and finalizes recording; it never resumes automatically. Saving is
independent of the old view and consumes against the current inspection and album IDs.

`pending-video-capture` holds capture identity and inspection/editing-album recovery
snapshots. Private recordings are validated for a nonempty video and audio track,
duration and size before streaming publication. Invalid/incomplete recordings can
explicitly be discarded or retried. `video-publications` makes publication retries
idempotent, including interruption between reserving and copying a MediaStore row.
Published clips live under `Movies/Inspecciones/<inspectionId>/` with unique MP4 names.
Discard removes only the new private recording, never an earlier public copy.

`local-media-index.json` is a versioned atomic sidecar keyed by inspection/album IDs.
Existing photos seed in their album order; later captures append in mixed order.
Photo entries reconcile against the current album, while unavailable video entries
retain their positions. This index is independent of serialized inspection fields:
videos never enter Album.getFotos(), photo counters, remote names or the FTP queue.
Form clearing, damage removal and draft removal after submission do not delete public
video bytes. Submitted videos remain accessible in the device gallery.

Mixed browsing retains photo zoom and adds video duration/play badges and Media3
controls. Clips start paused and restore selected reference/playback position after
recreation. Swiping away, returning to the grid or backgrounding releases the unused
player while preserving its position. The same admission rules apply to deletion of
both kinds. `pending-media-deletion` retains completion if metadata saving fails after
byte deletion; the toolbar action retries metadata without deleting bytes twice.

Automation uses the configured JBR. For the larger video dependency graph, validation
allocates more Gradle heap without changing project settings:

```sh
JAVA_HOME=/home/juanb/.jdks/jbr-21.0.11 ./gradlew --no-daemon \
  -Dorg.gradle.jvmargs='-Xmx3072m -XX:MaxMetaspaceSize=1024m' --max-workers=2 \
  assembleDebug assembleDebugAndroidTest testDebugUnitTest lintDebug assembleRelease
adb shell pm grant com.juans.inspeccion android.permission.RECORD_AUDIO
adb shell am instrument -w -e suite video com.juans.inspeccion.test/com.juans.inspeccion.Interfaz.Gallery.GalleryInstrumentation
```

Physical microphone fidelity, production-device camera quirks, authenticated server
submission/FTP and printing require manual acceptance. Emulator tests inspect actual
MP4 audio tracks but cannot establish physical microphone quality. Unit tests exercise
both limit-finalization events and the exact configured limits; full-duration/200 MB
recordings on physical devices remain untested.

Rollback uses an isolated checkout reverting only the video commit. Install with
`adb install -r` using the same debug identity; do not uninstall or clear application
data, since those operations remove the local index/drafts. The photo checkpoint
ignores video sidecars; upgrading again restores mixed browsing. Explicitly deleted
media cannot be recovered by reverting code.

Video checkpoint automated checks pass with all 94 JVM tests; debug/release assembly
and lint pass with no new lint errors (existing baseline remains). The video device
suite passes 37 checks on each API 29/34 emulator: all four workflows, real MP4/audio
tracks, mixed ordering, paused playback/restoration, deletion, unsaved damage albums,
interrupted recording, discard, final-video deletion, 120-item albums and unavailable
videos. Camera and photo-gallery regression results and the isolated rollback/update
results are recorded with the checkpoint artifacts outside the repository.
