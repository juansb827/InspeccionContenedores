package com.juans.inspeccion.Interfaz.Camera;

import android.os.*;
import android.view.*;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import com.juans.inspeccion.Interfaz.Gallery.GalleryAlbums;
import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.Mundo.Storage.*;
import com.juans.inspeccion.R;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.*;
import java.io.IOException;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29, application = android.app.Application.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class CameraCaptureFragmentTest {
    private ActivityController<Host> controller;
    private Host host;
    private CameraCaptureFragment camera;
    @Before public void setup() {
        controller = Robolectric.buildActivity(Host.class).setup().visible(); host = controller.get();
        camera = CameraCaptureFragment.newInstance(host.inspection.getPhotoId(), host.editing.getId(), 0);
        camera.showNow(host.getSupportFragmentManager(), "camera");
        Shadows.shadowOf(Looper.getMainLooper()).idle();
    }
    @After public void cleanup() { controller.pause().stop().destroy(); }
    @Test public void deniedPermissionKeepsFormAndCreatesNoMedia() {
        assertFalse(camera.requireView().findViewById(R.id.cameraShutter).isEnabled());
        assertEquals(host.getString(R.string.camera_permission_denied), ((TextView) camera.requireView().findViewById(R.id.cameraMessage)).getText().toString());
        assertNull(host.model.coordinator().session()); assertTrue(host.editing.getFotos().isEmpty());
    }
    @Test public void cameraStartsInPhotoAndVideoPermissionDenialLeavesPhotoAvailable() {
        android.widget.RadioButton photo=camera.requireView().findViewById(R.id.cameraPhoto);
        assertTrue(photo.isChecked()); camera.requireView().findViewById(R.id.cameraVideo).performClick();
        assertFalse(camera.requireView().findViewById(R.id.cameraShutter).isEnabled());
        camera.requireView().findViewById(R.id.cameraPhoto).performClick(); assertTrue(photo.isChecked());
        assertNull(host.model.coordinator().session()); assertTrue(host.editing.getFotos().isEmpty());
    }
    @Test public void backReturnsToSameFormWithoutInspectionLeaveConfirmation() {
        camera.requireDialog().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        camera.requireDialog().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        host.getSupportFragmentManager().executePendingTransactions();
        assertNull(host.getSupportFragmentManager().findFragmentByTag("camera")); assertEquals(0, host.leaves);
    }
    @Test public void editingAlbumContextAndInspectionValuesSurviveRecreation() {
        host.inspection.getInformacion().put("test-value", "unsaved-value"); String albumId = host.editing.getId();
        controller.recreate(); host = controller.get(); camera = (CameraCaptureFragment) host.getSupportFragmentManager().findFragmentByTag("camera");
        assertEquals("Daño en edición", ((TextView) camera.requireView().findViewById(R.id.cameraContext)).getText().toString());
        assertEquals(albumId, host.editing.getId()); assertEquals("unsaved-value", host.inspection.getInformacion().get("test-value"));
        assertNull(host.model.coordinator().session());
    }
    public static class Host extends FragmentActivity implements CameraHost {
        Inspeccion inspection; Album editing; CameraCaptureModel model; int leaves;
        protected void onCreate(Bundle state) {
            inspection = state == null ? new Inspeccion() : (Inspeccion) state.getSerializable("inspection");
            editing = state == null ? new Album() : (Album) state.getSerializable("editing");
            setTheme(R.style.CustomActionBarTheme); super.onCreate(state);
            model = new ViewModelProvider(this).get(CameraCaptureModel.class);
            try { model.attach(new CaptureCoordinator(new MediaPhotoStore(this), getFileStreamPath("pending-photo-capture"))); } catch (IOException e) { throw new IllegalStateException(e); }
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) { public void handleOnBackPressed() { leaves++; } });
        }
        protected void onSaveInstanceState(Bundle out) { super.onSaveInstanceState(out); out.putSerializable("inspection", inspection); out.putSerializable("editing", editing); }
        public Inspeccion cameraInspection() { return inspection; }
        public boolean canCaptureMedia(String i, String a) { return GalleryAlbums.canDelete(inspection, editing, i, a, false, null, model.busy()); }
        public void cameraMediaAssociated() { }
        public Album resolveGalleryAlbum(String i, String a) { return GalleryAlbums.resolve(inspection, editing, i, a); }
        public String galleryAlbumLabel(String a) { return GalleryAlbums.label(inspection, a); }
        public boolean canDeleteGalleryPhoto(String i, String a) { return false; }
        public void galleryPhotoDeleted(String i, String a, String ref) { }
    }
}
