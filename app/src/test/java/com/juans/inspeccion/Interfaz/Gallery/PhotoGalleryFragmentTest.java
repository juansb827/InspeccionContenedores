package com.juans.inspeccion.Interfaz.Gallery;

import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import android.view.KeyEvent;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.viewpager2.widget.ViewPager2;
import com.juans.inspeccion.Mundo.*;
import com.juans.inspeccion.R;
import org.junit.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;
import static org.junit.Assert.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 29, application = android.app.Application.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class PhotoGalleryFragmentTest {
    private ActivityController<HostActivity> controller;
    private HostActivity activity;
    private PhotoGalleryFragment gallery;
    @Before public void setup() {
        controller = Robolectric.buildActivity(HostActivity.class).setup().visible(); activity = controller.get();
    }
    @After public void cleanup() { controller.pause().stop().destroy(); }
    private void show(Album album) {
        gallery = PhotoGalleryFragment.newInstance(activity.inspection.getPhotoId(), album.getId());
        gallery.showNow(activity.getSupportFragmentManager(), "photos");
        Shadows.shadowOf(Looper.getMainLooper()).idle();
    }
    private void open(int position) {
        RecyclerView grid = gallery.requireView().findViewById(R.id.galleryGrid);
        grid.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(900, View.MeasureSpec.EXACTLY));
        grid.layout(0, 0, 1080, 900);
        grid.findViewHolderForAdapterPosition(position).itemView.performClick();
        Shadows.shadowOf(Looper.getMainLooper()).idle();
    }
    private void missingPhotos(Album album, int count) {
        for (int i = 0; i < count; i++) album.getFotos().add("/old/Inspecciones/missing-" + i + ".jpg");
    }

    @Test public void emptyAndMissingAlbumsShowMessagesAndBackDoesNotLeaveInspection() {
        show(activity.inspection.getFotosGenerales());
        assertEquals(View.VISIBLE, gallery.requireView().findViewById(R.id.galleryEmpty).getVisibility());
        assertEquals(View.GONE, gallery.requireView().findViewById(R.id.galleryDelete).getVisibility());
        gallery.requireDialog().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK));
        gallery.requireDialog().dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK));
        activity.getSupportFragmentManager().executePendingTransactions();
        assertNull(activity.getSupportFragmentManager().findFragmentByTag("photos")); assertEquals(0, activity.leaves);
        show(new Album());
        assertEquals(activity.getString(R.string.gallery_missing), ((TextView) gallery.requireView().findViewById(R.id.galleryEmpty)).getText().toString());
    }

    @Test public void unavailablePhotoCanOpenAndBackReturnsToGrid() {
        Album album = activity.inspection.getFotosGenerales(); missingPhotos(album, 3); show(album); open(1);
        assertEquals("2 de 3", ((TextView) gallery.requireView().findViewById(R.id.galleryTitle)).getText().toString());
        assertEquals(View.VISIBLE, gallery.requireView().findViewById(R.id.galleryPager).getVisibility());
        gallery.requireView().findViewById(R.id.galleryBack).performClick();
        assertEquals(View.VISIBLE, gallery.requireView().findViewById(R.id.galleryGrid).getVisibility());
        assertEquals(0, activity.leaves);
    }

    @Test public void selectedPhotoAndUnsavedAlbumSurviveActivityRecreation() {
        Album album = activity.editing; missingPhotos(album, 12); show(album); open(2);
        ((ViewPager2) gallery.requireView().findViewById(R.id.galleryPager)).setCurrentItem(7, false);
        controller.recreate(); activity = controller.get();
        gallery = (PhotoGalleryFragment) activity.getSupportFragmentManager().findFragmentByTag("photos");
        Shadows.shadowOf(Looper.getMainLooper()).idle();
        assertEquals("8 de 12", ((TextView) gallery.requireView().findViewById(R.id.galleryTitle)).getText().toString());
        assertTrue(((TextView) gallery.requireView().findViewById(R.id.galleryContext)).getText().toString().startsWith("Daño en edición"));
        gallery.requireView().findViewById(R.id.galleryBack).performClick();
        assertEquals(View.VISIBLE, gallery.requireView().findViewById(R.id.galleryGrid).getVisibility());
    }

    @Test public void readOnlyBrowsingShowsViewerWithoutDeleteAction() {
        activity.readOnly = true; missingPhotos(activity.inspection.getFotosGenerales(), 1);
        show(activity.inspection.getFotosGenerales()); open(0);
        assertEquals(View.VISIBLE, gallery.requireView().findViewById(R.id.galleryPager).getVisibility());
        assertEquals(View.GONE, gallery.requireView().findViewById(R.id.galleryDelete).getVisibility());
    }

    @Test public void gridAnchorSurvivesViewerNavigationAndRecreation() {
        missingPhotos(activity.inspection.getFotosGenerales(), 48); show(activity.inspection.getFotosGenerales());
        RecyclerView grid = gallery.requireView().findViewById(R.id.galleryGrid);
        grid.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(900, View.MeasureSpec.EXACTLY));
        grid.layout(0, 0, 1080, 900); grid.scrollBy(0, 1080);
        int anchor = ((GridLayoutManager) grid.getLayoutManager()).findFirstVisibleItemPosition();
        assertTrue(anchor > 0); open(anchor);
        controller.recreate(); activity = controller.get();
        gallery = (PhotoGalleryFragment) activity.getSupportFragmentManager().findFragmentByTag("photos");
        gallery.requireView().findViewById(R.id.galleryBack).performClick();
        grid = gallery.requireView().findViewById(R.id.galleryGrid);
        grid.measure(View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY), View.MeasureSpec.makeMeasureSpec(900, View.MeasureSpec.EXACTLY));
        grid.layout(0, 0, 1080, 900);
        assertEquals(anchor, ((GridLayoutManager) grid.getLayoutManager()).findFirstVisibleItemPosition());
    }

    public static class HostActivity extends FragmentActivity implements GalleryHost {
        Inspeccion inspection; Album editing; boolean readOnly; int leaves;
        @Override protected void onCreate(Bundle state) {
            inspection = state == null ? new Inspeccion() : (Inspeccion) state.getSerializable("test-inspection");
            editing = state == null ? new Album() : (Album) state.getSerializable("test-editing");
            setTheme(R.style.CustomActionBarTheme); super.onCreate(state);
            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                public void handleOnBackPressed() { leaves++; }
            });
        }
        @Override protected void onSaveInstanceState(Bundle state) {
            super.onSaveInstanceState(state); state.putSerializable("test-inspection", inspection); state.putSerializable("test-editing", editing);
        }
        public Album resolveGalleryAlbum(String inspectionId, String albumId) { return GalleryAlbums.resolve(inspection, editing, inspectionId, albumId); }
        public String galleryAlbumLabel(String albumId) { return GalleryAlbums.label(inspection, albumId); }
        public boolean canDeleteGalleryPhoto(String inspectionId, String albumId) {
            return GalleryAlbums.canDelete(inspection, editing, inspectionId, albumId, readOnly, null, false);
        }
        public void galleryPhotoDeleted(String inspectionId, String albumId, String reference) {
            Album album = resolveGalleryAlbum(inspectionId, albumId); if (album != null) album.removePhotoReference(reference);
        }
    }
}
