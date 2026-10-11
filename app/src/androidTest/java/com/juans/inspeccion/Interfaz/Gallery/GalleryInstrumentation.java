package com.juans.inspeccion.Interfaz.Gallery;

import android.app.*;
import android.graphics.Bitmap;
import android.content.Intent;
import android.os.*;
import android.view.*;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;
import com.github.chrisbanes.photoview.PhotoView;
import com.juans.inspeccion.Mundo.Album;
import com.juans.inspeccion.R;
import java.lang.reflect.Field;
import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;

/** Standalone local acceptance runner, invoked with adb shell am instrument -w. */
public class GalleryInstrumentation extends com.juans.inspeccion.Interfaz.Camera.CameraInstrumentation {
    private boolean cameraSuite;
    private volatile GalleryDeviceHost current;
    private int checks;
    @Override public void onCreate(Bundle args) { cameraSuite = args != null && "camera".equals(args.getString("suite")); super.onCreate(args); }
    @Override public void onStart() {
        if (cameraSuite) { super.onStart(); return; }
        Bundle report = new Bundle();
        try {
            ((Application) getTargetContext().getApplicationContext()).registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
                public void onActivityResumed(Activity activity) { if (activity instanceof GalleryDeviceHost) current = (GalleryDeviceHost) activity; }
                public void onActivityCreated(Activity a, Bundle b) {} public void onActivityStarted(Activity a) {}
                public void onActivityPaused(Activity a) {} public void onActivityStopped(Activity a) {}
                public void onActivitySaveInstanceState(Activity a, Bundle b) {} public void onActivityDestroyed(Activity a) {}
            });
            for (String document : new String[]{"EP", "ET"}) for (String direction : new String[]{"ENTRADA", "SALIDA"}) {
                int count = checks == 0 ? 48 : 8;
                launch(document, direction, "general", false, count);
                if (checks == 0) screenshot("gallery-grid");
                open(0); waitFor(() -> ui(() -> photo() != null && photo().getDrawable() instanceof android.graphics.drawable.BitmapDrawable));
                if (checks == 0) screenshot("gallery-viewer");
                check(ui(() -> title().equals("1 de " + (count + 1))), "photo counter");
                doubleTap(); waitFor(() -> ui(() -> photo().getScale() > 1.5f));
                doubleTap(); waitFor(() -> ui(() -> photo().getScale() < 1.05f));
                pinch(); waitFor(() -> ui(() -> photo().getScale() > 1.2f));
                check(ui(() -> title().equals("1 de " + (count + 1))), "pinch stays on current photo");
                float beforePan = ui(() -> photo().getDisplayRect().left);
                pan(); check(ui(() -> Math.abs(photo().getDisplayRect().left - beforePan) > 1), "zoomed photo pans");
                doubleTap(); waitFor(() -> ui(() -> photo().getScale() < 1.05f));
                swipe(); waitFor(() -> ui(() -> title().equals("2 de " + (count + 1))));
                String removed = ui(() -> current.selectedAlbum().getFotos().get(1));
                runOnMainSync(() -> gallery().requireView().findViewById(R.id.galleryDelete).performClick());
                runOnMainSync(() -> confirmation().getButton(AlertDialog.BUTTON_POSITIVE).performClick());
                waitFor(() -> ui(() -> current.persisted == 1));
                check(ui(() -> !current.selectedAlbum().getFotos().contains(removed)), "deleted reference");
                check(ui(() -> title().equals("2 de " + count)), "next photo after deletion");
                GalleryDeviceHost old = current;
                runOnMainSync(() -> ((ViewPager2) gallery().requireView().findViewById(R.id.galleryPager)).setCurrentItem(7, false));
                runOnMainSync(() -> current.recreate()); waitFor(() -> current != old && ui(() -> gallery() != null));
                check(ui(() -> title().equals("8 de " + count)), "restored selection");
                sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                check(ui(() -> gallery().requireView().findViewById(R.id.galleryGrid).getVisibility() == View.VISIBLE), "back to grid");
                sendKeyDownUpSync(KeyEvent.KEYCODE_BACK); waitFor(() -> ui(() -> gallery() == null));
                runOnMainSync(() -> current.finish()); waitForIdleSync();
            }
            launch("EP", "SALIDA", "damage", false, 2); open(0);
            check(ui(() -> gallery().requireView().findViewById(R.id.galleryDelete).getVisibility() == View.GONE), "Salida damage is browse-only");
            runOnMainSync(() -> current.finish()); waitForIdleSync();
            launch("EP", "ENTRADA", "editing", true, 2); open(0);
            check(ui(() -> gallery().requireView().findViewById(R.id.galleryDelete).getVisibility() == View.GONE), "read-only editing album");
            check(ui(() -> ((TextView) gallery().requireView().findViewById(R.id.galleryContext)).getText().toString().contains("Daño en edición")), "editing album context");
            runOnMainSync(() -> current.finish()); waitForIdleSync();
            launch("ET", "ENTRADA", "general", false, 1, false); open(0);
            runOnMainSync(() -> gallery().requireView().findViewById(R.id.galleryDelete).performClick());
            runOnMainSync(() -> confirmation().getButton(AlertDialog.BUTTON_POSITIVE).performClick());
            waitFor(() -> ui(() -> current.selectedAlbum().getFotos().isEmpty()));
            check(ui(() -> gallery().requireView().findViewById(R.id.galleryEmpty).getVisibility() == View.VISIBLE), "final deletion opens empty gallery");
            runOnMainSync(() -> current.finish()); waitForIdleSync();
            launch("ET", "ENTRADA", "general", false, 0); open(0);
            check(ui(() -> title().equals("1 de 1")), "unavailable item stays aligned");
            report.putString("stream", "PASS: " + checks + " gallery checks on API " + Build.VERSION.SDK_INT + "\n");
            finish(Activity.RESULT_OK, report);
        } catch (Throwable error) {
            report.putString("stream", "FAIL after " + checks + " checks: " + android.util.Log.getStackTraceString(error));
            finish(Activity.RESULT_CANCELED, report);
        }
    }

    private void launch(String document, String direction, String kind, boolean readOnly, int count) {
        launch(document, direction, kind, readOnly, count, true);
    }
    private void launch(String document, String direction, String kind, boolean readOnly, int count, boolean unavailable) {
        Intent intent = new Intent(getTargetContext(), GalleryDeviceHost.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .putExtra("document", document).putExtra("direction", direction).putExtra("kind", kind)
                .putExtra("readOnly", readOnly).putExtra("count", count).putExtra("unavailable", unavailable);
        current = (GalleryDeviceHost) startActivitySync(intent);
        waitFor(() -> ui(() -> gallery() != null && gallery().getView() != null));
        waitForIdleSync();
    }
    private PhotoGalleryFragment gallery() { return (PhotoGalleryFragment) current.getSupportFragmentManager().findFragmentByTag("photos"); }
    private String title() { return ((TextView) gallery().requireView().findViewById(R.id.galleryTitle)).getText().toString(); }
    private void open(int position) {
        waitForIdleSync(); runOnMainSync(() -> {
            RecyclerView grid = gallery().requireView().findViewById(R.id.galleryGrid);
            RecyclerView.ViewHolder holder = grid.findViewHolderForAdapterPosition(position);
            if (holder == null) throw new AssertionError("Missing grid tile " + position);
            holder.itemView.performClick();
        }); waitForIdleSync();
    }
    private PhotoView photo() {
        ViewPager2 pager = gallery().requireView().findViewById(R.id.galleryPager);
        RecyclerView list = (RecyclerView) pager.getChildAt(0);
        RecyclerView.ViewHolder holder = list.findViewHolderForAdapterPosition(pager.getCurrentItem());
        return holder == null ? null : (PhotoView) ((ViewGroup) holder.itemView).getChildAt(0);
    }
    private AlertDialog confirmation() {
        try { Field field = PhotoGalleryFragment.class.getDeclaredField("confirmation"); field.setAccessible(true); return (AlertDialog) field.get(gallery()); }
        catch (ReflectiveOperationException e) { throw new AssertionError(e); }
    }
    private void doubleTap() {
        int[] xy = ui(() -> { int[] p = new int[2]; photo().getLocationOnScreen(p); p[0] += photo().getWidth()/2; p[1] += photo().getHeight()/2; return p; });
        long time = SystemClock.uptimeMillis();
        // Keep event timestamps within Android's double-tap interval even when
        // an emulator's rendering briefly delays synchronous input injection.
        pointer(time, time, MotionEvent.ACTION_DOWN, xy[0], xy[1]);
        pointer(time, time+60, MotionEvent.ACTION_UP, xy[0], xy[1]);
        pointer(time+160, time+160, MotionEvent.ACTION_DOWN, xy[0], xy[1]);
        pointer(time+160, time+220, MotionEvent.ACTION_UP, xy[0], xy[1]);
        SystemClock.sleep(400);
    }
    private void pinch() {
        int[] center = ui(() -> { int[] p = new int[4]; photo().getLocationOnScreen(p); p[0] += photo().getWidth()/2; p[1] += photo().getHeight()/2; p[2] = ViewConfiguration.get(current).getScaledMinimumScalingSpan(); p[3] = photo().getWidth(); return p; });
        Bundle diagnostic = new Bundle(); diagnostic.putString("stream", "Pinch span threshold=" + center[2] + ", width=" + center[3] + "\n"); sendStatus(0, diagnostic);
        long start = SystemClock.uptimeMillis();
        pointer(start, start, MotionEvent.ACTION_DOWN, center[0]-80, center[1]);
        multiPointer(start, start+20, MotionEvent.ACTION_POINTER_DOWN | (1 << 8), center[0], center[1], 80);
        for (int i=1; i<=12; i++) multiPointer(start, start+20+i*20, MotionEvent.ACTION_MOVE, center[0], center[1], 80+i*25);
        multiPointer(start, start+280, MotionEvent.ACTION_POINTER_UP | (1 << 8), center[0], center[1], 380);
        pointer(start, start+300, MotionEvent.ACTION_UP, center[0]-380, center[1]); SystemClock.sleep(400);
    }
    private void multiPointer(long down, long time, int action, float x, float y, float distance) {
        if (time > SystemClock.uptimeMillis()) SystemClock.sleep(time - SystemClock.uptimeMillis());
        MotionEvent.PointerProperties[] properties = new MotionEvent.PointerProperties[2];
        MotionEvent.PointerCoords[] coordinates = new MotionEvent.PointerCoords[2];
        for (int i=0; i<2; i++) {
            properties[i] = new MotionEvent.PointerProperties(); properties[i].id = i; properties[i].toolType = MotionEvent.TOOL_TYPE_FINGER;
            coordinates[i] = new MotionEvent.PointerCoords(); coordinates[i].x = x+(i==0 ? -distance : distance);
            coordinates[i].y = y; coordinates[i].pressure = 1; coordinates[i].size = 1;
        }
        MotionEvent event = MotionEvent.obtain(down, time, action, 2, properties, coordinates, 0, 0, 1, 1, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        sendPointerSync(event); event.recycle();
    }
    private void swipe() {
        int[] rect = ui(() -> { int[] p = new int[4]; photo().getLocationOnScreen(p); p[2] = photo().getWidth(); p[3] = photo().getHeight(); return p; });
        long start = SystemClock.uptimeMillis(); float y = rect[1] + rect[3]*0.5f;
        pointer(start, start, MotionEvent.ACTION_DOWN, rect[0]+rect[2]*0.85f, y);
        for (int i=1; i<=12; i++) pointer(start, start+i*20, MotionEvent.ACTION_MOVE, rect[0]+rect[2]*(0.85f-i*0.7f/12), y);
        pointer(start, start+260, MotionEvent.ACTION_UP, rect[0]+rect[2]*0.15f, y); SystemClock.sleep(500);
    }
    private void pan() {
        int[] xy = ui(() -> { int[] p = new int[2]; photo().getLocationOnScreen(p); p[0] += photo().getWidth()/2; p[1] += photo().getHeight()/2; return p; });
        long start = SystemClock.uptimeMillis(); pointer(start, start, MotionEvent.ACTION_DOWN, xy[0], xy[1]);
        for (int i=1; i<=10; i++) pointer(start, start+i*30, MotionEvent.ACTION_MOVE, xy[0]+i*10, xy[1]);
        pointer(start, start+330, MotionEvent.ACTION_UP, xy[0]+100, xy[1]); SystemClock.sleep(400);
    }
    private void pointer(long down, long time, int action, float x, float y) {
        if (time > SystemClock.uptimeMillis()) SystemClock.sleep(time - SystemClock.uptimeMillis());
        MotionEvent.PointerProperties property = new MotionEvent.PointerProperties(); property.id = 0; property.toolType = MotionEvent.TOOL_TYPE_FINGER;
        MotionEvent.PointerCoords coordinate = new MotionEvent.PointerCoords(); coordinate.x = x; coordinate.y = y; coordinate.pressure = 1; coordinate.size = 1;
        MotionEvent event = MotionEvent.obtain(down, time, action, 1, new MotionEvent.PointerProperties[]{property},
                new MotionEvent.PointerCoords[]{coordinate}, 0, 0, 1, 1, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0);
        sendPointerSync(event); event.recycle();
    }
    private void check(boolean okay, String label) { if (!okay) throw new AssertionError(label); checks++; }
    private void waitFor(BooleanSupplier condition) {
        for (int i=0; i<300; i++) { if (condition.getAsBoolean()) return; SystemClock.sleep(100); }
        throw new AssertionError("Timed out waiting for gallery state: " + ui(() ->
                gallery() == null ? "no gallery, photos=" + current.selectedAlbum().getFotos().size()
                        + ", finishing=" + current.isFinishing() : title() + ", scale=" + (photo() == null ? "no photo" : photo().getScale())));
    }
    private <T> T ui(java.util.concurrent.Callable<T> action) {
        AtomicReference<T> value = new AtomicReference<>(); AtomicReference<Throwable> error = new AtomicReference<>();
        runOnMainSync(() -> { try { value.set(action.call()); } catch (Throwable failure) { error.set(failure); } });
        if (error.get() != null) throw new AssertionError(error.get()); return value.get();
    }
    private void screenshot(String name) {
        SystemClock.sleep(500);
        Bitmap bitmap = getUiAutomation().takeScreenshot();
        try (FileOutputStream out = new FileOutputStream(new File(getTargetContext().getFilesDir(), name + "-api" + Build.VERSION.SDK_INT + ".png"))) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (java.io.IOException e) { throw new AssertionError(e); }
        finally { bitmap.recycle(); }
    }
}
