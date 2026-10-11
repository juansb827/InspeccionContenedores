# Photo gallery

The Android 10 minimum and gallery are separate commits. The baseline is
`30079d6`: minimum SDK 29, original photo dialog, unchanged capture/storage
behavior and dependencies. Its APK and build log are outside the repository at
`~/.local/state/inspeccion-validation/2026-10-11/`.

## Behavior

`InspeccionActivity.showPhotoAlbum(Album)` opens a full-window gallery in the
same activity. The inspection ID and album ID resolve the current general,
saved-damage or unsaved editing album. Photos stay in their original order.
Available albums can be browsed in read-only inspections and Salida damage
screens; capture rules remain unchanged.

Tap a thumbnail to open the dark viewer; swipe between photos, pinch to zoom,
double-tap between 1× and 2×, and pan while zoomed. Maximum zoom is 4×. Back
returns to the grid at its previous position, then to the inspection's current
tab. Selection and grid position survive activity recreation. Unavailable
photos keep their positions and show a message instead of shifting indices.

Deletion requires an editable inspection, no restricted action and no capture,
printing or deletion in progress. Salida damage albums are browse-only.
Confirmation explicitly includes deletion from the device's photo library.
Storage deletion runs in a retained ViewModel using the reference, not an
index. Completion resolves the recreated owner's album and persists the change.
Failed storage deletion retains the reference and remote name. Existing
persistence recovery messages remain in use. After the final photo is deleted,
the empty gallery stays open.

`GalleryMediaItem` is a presentation model with reference/ID, MIME type and
media kind. No serialized inspection fields or identifiers have changed.
Video recording, playback, upload names and storage migration are deferred.

## Dependencies and checks

RecyclerView 1.3.2, ViewPager2 1.1.0, Glide 4.16.0 and PhotoView 2.3.0 are
pinned. JitPack serves only `com.github.chrisbanes`; other dependencies still
come from Google/Maven Central. Compile SDK remains 34 and target SDK 33.

```sh
JAVA_HOME=/home/juanb/.jdks/jbr-21.0.11 ./gradlew assembleDebug testDebugUnitTest lintDebug
JAVA_HOME=/home/juanb/.jdks/jbr-21.0.11 ./gradlew assembleDebugAndroidTest
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb install -r -t app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb shell am instrument -w com.juans.inspeccion.test/com.juans.inspeccion.Interfaz.Gallery.GalleryInstrumentation
```

The custom acceptance runner uses a non-exported **debug-only** host and local
MediaStore photo fixtures. It tests all four document/direction combinations,
damage/editing albums, read-only browsing, gestures, deletion, recreation,
navigation, unavailable files and the empty state. It never logs into production
or accesses a physical camera/printer. The host is absent from release APKs.
Full inspection-screen acceptance still requires test business data and services.

Validation on 2026-10-11 passed debug assembly, all 67 JVM tests and lint with no new
lint errors. The local acceptance runner passed 33 checks on API 29 and API 34.
The unsigned release build also passed, and its manifest excludes the debug
fixture activity. Logs, APKs, screenshots and a gallery-edited serialized
inspection are retained in the external validation directory above.

The baseline APK was installed on the API 29 emulator and its login screen
launched successfully. Full authenticated inspection flows, actual camera
capture/cancellation, physical printing and uploads were not exercised on
devices; no test credentials or business-service configuration were supplied.
Local gallery fixtures do not establish those separate device acceptance checks.

## Rollback

Revert only the gallery commit to restore the original viewer while keeping
minimum SDK 29. If it is the latest commit, `git revert HEAD` performs that
rollback. Keep the same application ID and signing identity for an in-place
test update; uninstalling clears private drafts and may change media ownership.
Gallery-edited drafts keep the original serialization format. Reverting code
does not restore explicitly deleted photos.
