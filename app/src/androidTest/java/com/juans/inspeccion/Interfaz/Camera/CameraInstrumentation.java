package com.juans.inspeccion.Interfaz.Camera;

import android.app.*;
import android.content.Intent;
import android.os.*;
import com.juans.inspeccion.Interfaz.Gallery.GalleryDeviceHost;
import com.juans.inspeccion.R;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

/** Actual CameraX capture on emulator hardware, without production credentials. */
public class CameraInstrumentation extends Instrumentation {
    private volatile GalleryDeviceHost current;
    protected int checks;
    @Override public void onCreate(Bundle args) { super.onCreate(args); start(); }
    @Override public void onStart() {
        Bundle report = new Bundle();
        try {
            ((Application) getTargetContext().getApplicationContext()).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                public void onActivityResumed(Activity a) { if (a instanceof GalleryDeviceHost) current = (GalleryDeviceHost) a; }
                public void onActivityCreated(Activity a, Bundle b) {} public void onActivityStarted(Activity a) {}
                public void onActivityPaused(Activity a) {} public void onActivityStopped(Activity a) {}
                public void onActivitySaveInstanceState(Activity a, Bundle b) {} public void onActivityDestroyed(Activity a) {}
            });
            for (String document : new String[]{"EP", "ET"}) for (String direction : new String[]{"ENTRADA", "SALIDA"}) {
                launch(document, direction, "general");
                waitFor(() -> ui(() -> camera() != null && camera().requireView().findViewById(R.id.cameraShutter).isEnabled()));
                check(ui(() -> current.capture.coordinator().session() == null), "no destination before shutter");
                runOnMainSync(() -> camera().requireView().findViewById(R.id.cameraShutter).performClick());
                waitFor(() -> ui(() -> current.persisted == 1 && camera() == null));
                check(ui(() -> current.selectedAlbum().getFotos().size() == 1), document + "/" + direction + " photo associated");
                check(ui(() -> current.capture.coordinator().session().finished), "completed recovery snapshot");
            }
            launch("EP", "ENTRADA", "editing");
            waitFor(() -> ui(() -> camera() != null && camera().requireView().findViewById(R.id.cameraShutter).isEnabled()));
            GalleryDeviceHost old = current; runOnMainSync(() -> old.recreate());
            waitFor(() -> current != old && ui(() -> camera() != null && camera().requireView().findViewById(R.id.cameraShutter).isEnabled()));
            runOnMainSync(() -> camera().requireView().findViewById(R.id.cameraShutter).performClick());
            waitFor(() -> ui(() -> current.persisted == 1 && camera() == null));
            check(ui(() -> current.editing.getFotos().size() == 1 && current.inspection.getFotosGenerales().getFotos().isEmpty()), "editing album survives recreation");
            launch("ET", "ENTRADA", "general");
            waitFor(() -> ui(() -> camera() != null));
            runOnMainSync(() -> camera().requireView().findViewById(R.id.cameraBack).performClick());
            waitFor(() -> ui(() -> camera() == null));
            check(ui(() -> current.selectedAlbum().getFotos().isEmpty() && current.capture.coordinator().session() == null), "cancel without capture");
            report.putString("stream", "PASS: " + checks + " embedded camera checks on API " + Build.VERSION.SDK_INT + "\n"); finish(Activity.RESULT_OK, report);
        } catch (Throwable e) {
            report.putString("stream", android.util.Log.getStackTraceString(e)); finish(Activity.RESULT_CANCELED, report);
        }
    }
    private void launch(String document, String direction, String kind) {
        // A new fixture and journal for each test; completed public photos are retained.
        getTargetContext().deleteFile("fixture-photo-capture");
        Intent intent = new Intent(getTargetContext(), GalleryDeviceHost.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra("document", document).putExtra("direction", direction).putExtra("kind", kind).putExtra("cameraOnly", true);
        current = (GalleryDeviceHost) startActivitySync(intent); waitForIdleSync();
    }
    private CameraCaptureFragment camera() { return (CameraCaptureFragment) current.getSupportFragmentManager().findFragmentByTag("camera"); }
    private void waitFor(BooleanSupplier condition) {
        for (int i = 0; i < 300; i++) { if (condition.getAsBoolean()) return; SystemClock.sleep(100); }
        throw new AssertionError("Camera state timeout: " + ui(() -> current.capture.state().phase + " " + current.capture.state().error));
    }
    private void check(boolean okay, String label) { if (!okay) throw new AssertionError(label); checks++; }
    private <T> T ui(java.util.concurrent.Callable<T> action) {
        AtomicReference<T> value = new AtomicReference<>(); AtomicReference<Throwable> failure = new AtomicReference<>();
        runOnMainSync(() -> { try { value.set(action.call()); } catch (Throwable e) { failure.set(e); } });
        if (failure.get() != null) throw new AssertionError(failure.get()); return value.get();
    }
}
